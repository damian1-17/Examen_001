package com.example.examen_001.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.examen_001.api.RetrofitClient;
import com.example.examen_001.database.DatabaseHelper;
import com.example.examen_001.models.ExchangeRateResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestor de tasas de cambio con cache en SQLite
 */
public class ExchangeRateManager {

    private static final String TAG = "ExchangeRateManager";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 horas en milisegundos

    private Context context;
    private DatabaseHelper dbHelper;

    public ExchangeRateManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Interface para callbacks de las tasas de cambio
     */
    public interface ExchangeRateCallback {
        void onSuccess(double rate);
        void onError(String error);
    }

    /**
     * Obtiene la tasa de cambio entre dos monedas
     */
    public void getExchangeRate(final String fromCurrency, final String toCurrency,
                                final ExchangeRateCallback callback) {

        // Si es la misma moneda, retornar 1.0
        if (fromCurrency.equals(toCurrency)) {
            callback.onSuccess(1.0);
            return;
        }

        // Intentar obtener del cache primero
        double cachedRate = getCachedRate(fromCurrency, toCurrency);
        if (cachedRate > 0) {
            Log.d(TAG, "Usando tasa desde cache: " + cachedRate);
            callback.onSuccess(cachedRate);
            return;
        }

        // Si no está en cache, obtener de la API
        fetchFromAPI(fromCurrency, toCurrency, callback);
    }

    /**
     * Obtiene tasa del cache si no ha expirado
     */
    private double getCachedRate(String fromCurrency, String toCurrency) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long currentTime = System.currentTimeMillis() / 1000;
        long cacheExpireTime = currentTime - (CACHE_DURATION / 1000);

        String query = "SELECT " + DatabaseHelper.COLUMN_RATE +
                " FROM " + DatabaseHelper.TABLE_EXCHANGE_RATES +
                " WHERE " + DatabaseHelper.COLUMN_BASE_CURRENCY + " = ?" +
                " AND " + DatabaseHelper.COLUMN_TARGET_CURRENCY + " = ?" +
                " AND " + DatabaseHelper.COLUMN_LAST_UPDATED + " > ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                fromCurrency, toCurrency, String.valueOf(cacheExpireTime)
        });

        double rate = 0;
        if (cursor.moveToFirst()) {
            rate = cursor.getDouble(0);
        }
        cursor.close();

        return rate;
    }

    /**
     * Obtiene las tasas desde la API
     */
    private void fetchFromAPI(final String fromCurrency, final String toCurrency,
                              final ExchangeRateCallback callback) {

        Log.d(TAG, "Obteniendo tasas desde API para: " + fromCurrency);

        RetrofitClient.getExchangeRateService()
                .getExchangeRates(fromCurrency)
                .enqueue(new Callback<ExchangeRateResponse>() {
                    @Override
                    public void onResponse(Call<ExchangeRateResponse> call,
                                           Response<ExchangeRateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ExchangeRateResponse rateResponse = response.body();

                            // Guardar todas las tasas en cache
                            saveRatesToCache(fromCurrency, rateResponse.getRates());

                            // Obtener la tasa específica solicitada
                            double rate = rateResponse.getRate(toCurrency);

                            Log.d(TAG, "Tasa obtenida: " + fromCurrency + " -> " + toCurrency + " = " + rate);
                            callback.onSuccess(rate);

                        } else {
                            String error = "Error en la respuesta: " + response.code();
                            Log.e(TAG, error);
                            callback.onError(error);
                        }
                    }

                    @Override
                    public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                        String error = "Error de conexión: " + t.getMessage();
                        Log.e(TAG, error, t);
                        callback.onError(error);
                    }
                });
    }

    /**
     * Guarda las tasas en el cache de SQLite
     */
    private void saveRatesToCache(String baseCurrency, Map<String, Double> rates) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long currentTime = System.currentTimeMillis() / 1000;

        db.beginTransaction();
        try {
            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                String targetCurrency = entry.getKey();
                double rate = entry.getValue();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_BASE_CURRENCY, baseCurrency);
                values.put(DatabaseHelper.COLUMN_TARGET_CURRENCY, targetCurrency);
                values.put(DatabaseHelper.COLUMN_RATE, rate);
                values.put(DatabaseHelper.COLUMN_LAST_UPDATED, currentTime);

                // Usar REPLACE para actualizar si existe
                db.replace(DatabaseHelper.TABLE_EXCHANGE_RATES, null, values);
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Cache actualizado con " + rates.size() + " tasas");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Convierte un monto entre dos monedas
     */
    public void convertAmount(double amount, String fromCurrency, String toCurrency,
                              final ConversionCallback callback) {

        getExchangeRate(fromCurrency, toCurrency, new ExchangeRateCallback() {
            @Override
            public void onSuccess(double rate) {
                double convertedAmount = amount * rate;
                callback.onSuccess(convertedAmount, rate);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Interface para callbacks de conversión
     */
    public interface ConversionCallback {
        void onSuccess(double convertedAmount, double rate);
        void onError(String error);
    }

    /**
     * Fuerza la actualización del cache desde la API
     */
    public void forceRefresh(String baseCurrency, final RefreshCallback callback) {
        Log.d(TAG, "Forzando actualización de cache para: " + baseCurrency);

        RetrofitClient.getExchangeRateService()
                .getExchangeRates(baseCurrency)
                .enqueue(new Callback<ExchangeRateResponse>() {
                    @Override
                    public void onResponse(Call<ExchangeRateResponse> call,
                                           Response<ExchangeRateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            saveRatesToCache(baseCurrency, response.body().getRates());
                            callback.onSuccess("Cache actualizado correctamente");
                        } else {
                            callback.onError("Error en la respuesta: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                        callback.onError("Error de conexión: " + t.getMessage());
                    }
                });
    }

    /**
     * Interface para callback de actualización
     */
    public interface RefreshCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Limpia el cache de tasas de cambio
     */
    public void clearCache() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_EXCHANGE_RATES, null, null);
        Log.d(TAG, "Cache limpiado");
    }

    /**
     * Obtiene la fecha de última actualización del cache
     */
    public long getLastUpdateTime(String baseCurrency) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT MAX(" + DatabaseHelper.COLUMN_LAST_UPDATED + ")" +
                " FROM " + DatabaseHelper.TABLE_EXCHANGE_RATES +
                " WHERE " + DatabaseHelper.COLUMN_BASE_CURRENCY + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{baseCurrency});

        long lastUpdate = 0;
        if (cursor.moveToFirst()) {
            lastUpdate = cursor.getLong(0);
        }
        cursor.close();

        return lastUpdate * 1000; // Convertir a milisegundos
    }
}