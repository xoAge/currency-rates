package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.Rate;

import java.math.BigDecimal;

/// Курс одной валюты в ответе API.
public record RateResponse(String code, String name, BigDecimal rubPerUnit) {

    public static RateResponse from(Rate rate) {
        return new RateResponse(rate.code(), rate.name(), rate.rubPerUnit());
    }
}
