package com.example.examen_001.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit para peticiones a la API
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static ExchangeRateService exchangeRateService = null;

    /**
     * Obtiene la instancia de Retrofit (Singleton)
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurar logging para debug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurar OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Configurar Gson
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Crear instancia de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(ExchangeRateService.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /**
     * Obtiene el servicio de tasas de cambio
     */
    public static ExchangeRateService getExchangeRateService() {
        if (exchangeRateService == null) {
            exchangeRateService = getClient().create(ExchangeRateService.class);
        }
        return exchangeRateService;
    }
}