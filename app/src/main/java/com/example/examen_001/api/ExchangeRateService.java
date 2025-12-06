package com.example.examen_001.api;

import com.example.examen_001.models.ExchangeRateResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Servicio API para obtener tasas de cambio
 * Usa ExchangeRate-API (gratuita)
 */
public interface ExchangeRateService {

    /**
     * Obtiene las tasas de cambio para una moneda base
     * @param baseCurrency Código de moneda base (USD, EUR, etc.)
     * @return Respuesta con todas las tasas de cambio
     */
    @GET("v4/latest/{currency}")
    Call<ExchangeRateResponse> getExchangeRates(@Path("currency") String baseCurrency);

    /**
     * URL base de la API
     */
    String BASE_URL = "https://api.exchangerate-api.com/";
}