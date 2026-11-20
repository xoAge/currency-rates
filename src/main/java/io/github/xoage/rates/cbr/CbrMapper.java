package io.github.xoage.rates.cbr;

import io.github.xoage.rates.rate.DailyRates;
import io.github.xoage.rates.rate.Rate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/// Переводит ответ ЦБ в нашу модель. Здесь же решается проблема номинала.
final class CbrMapper {
    /// Знаков после запятой у курса за единицу.
    private static final int RATE_SCALE = 8;

    private CbrMapper() {}

    static DailyRates toDailyRates(CbrDailyResponse response) {
        Map<String, Rate> rates = new HashMap<>();

        for (CbrValute valute : response.valute().values()) {
            BigDecimal rubPerUnit = valute.value()
                    .divide(BigDecimal.valueOf(valute.nominal()), RATE_SCALE, RoundingMode.HALF_UP);
            rates.put(valute.charCode(), new Rate(valute.charCode(), valute.name(), rubPerUnit));
        }

        rates.put("RUB", new Rate("RUB", "Российский рубль", BigDecimal.ONE));

        return new DailyRates(response.date().toLocalDate(), rates);
    }
}
