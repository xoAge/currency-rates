package io.github.xoage.rates.rate;

import java.math.BigDecimal;

/// Курс одной валюты: сколько рублей стоит ОДНА её единица.
public record Rate(String code, String name, BigDecimal rubPerUnit) {
}
