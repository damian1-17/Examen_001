package com.example.examen_001.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Modelo de respuesta de la API de tasas de cambio
 */
public class ExchangeRateResponse {

    @SerializedName("base")
    private String base;

    @SerializedName("date")
    private String date;

    @SerializedName("time_last_updated")
    private long timeLastUpdated;

    @SerializedName("rates")
    private Map<String, Double> rates;

    // Getters
    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public long getTimeLastUpdated() {
        return timeLastUpdated;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    /**
     * Obtiene la tasa de cambio para una moneda específica
     */
    public double getRate(String currency) {
        if (rates != null && rates.containsKey(currency)) {
            return rates.get(currency);
        }
        return 1.0;
    }

    /**
     * Convierte un monto de la moneda base a otra moneda
     */
    public double convert(double amount, String toCurrency) {
        return amount * getRate(toCurrency);
    }

    /**
     * Convierte entre dos monedas (no base)
     */
    public double convertBetween(double amount, String fromCurrency, String toCurrency) {
        // Primero convertir a la moneda base
        double inBase = amount / getRate(fromCurrency);
        // Luego a la moneda destino
        return inBase * getRate(toCurrency);
    }

    // Setters
    public void setBase(String base) {
        this.base = base;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeLastUpdated(long timeLastUpdated) {
        this.timeLastUpdated = timeLastUpdated;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}