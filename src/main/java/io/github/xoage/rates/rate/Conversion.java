package io.github.xoage.rates.rate;

import java.math.BigDecimal;
import java.time.LocalDate;

/// Результат конвертации: сколько, из чего, во что, сколько получилось и по курсу на какую дату.
public record Conversion(String from, String to, BigDecimal amount, BigDecimal result, LocalDate date) {
}
