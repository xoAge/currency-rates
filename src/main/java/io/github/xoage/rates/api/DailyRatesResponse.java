package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.DailyRates;
import io.github.xoage.rates.rate.Rate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/// Все курсы на дату, отсортированные по коду валюты.
public record DailyRatesResponse(LocalDate date, List<RateResponse> rates) {

    public static DailyRatesResponse from(DailyRates dailyRates) {
        List<RateResponse> rates = dailyRates.rates().values().stream()
                .sorted(Comparator.comparing(Rate::code))
                .map(RateResponse::from)
                .toList();
        return new DailyRatesResponse(dailyRates.date(), rates);
    }
}
