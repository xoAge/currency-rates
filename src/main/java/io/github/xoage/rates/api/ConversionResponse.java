package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.Conversion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConversionResponse(String from, String to, BigDecimal amount, BigDecimal result, LocalDate date) {

    public static ConversionResponse from(Conversion conversion) {
        return new ConversionResponse(conversion.from(), conversion.to(),
                conversion.amount(), conversion.result(), conversion.date());
    }
}
