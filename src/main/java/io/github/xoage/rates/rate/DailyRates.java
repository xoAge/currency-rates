package io.github.xoage.rates.rate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/// Курсы всех валют на одну дату. Ключ словаря - код валюты (USD, EUR...).
public record DailyRates(LocalDate date, Map<String, Rate> rates) {
    public DailyRates {
        rates = Map.copyOf(rates);
    }
    
    public Optional<Rate> find(String code) {
        return Optional.ofNullable(rates.get(code));
    }
}
