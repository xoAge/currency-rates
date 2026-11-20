package io.github.xoage.rates.cbr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/// Одна валюта в ответе ЦБ. Курс Value указан в рублях за Nominal единиц валюты.
record CbrValute(
        @JsonProperty("CharCode") String charCode,
        @JsonProperty("Nominal") int nominal,
        @JsonProperty("Name") String name,
        @JsonProperty("Value") BigDecimal value
) {
}
