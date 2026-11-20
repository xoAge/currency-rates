package io.github.xoage.rates.cbr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

/// Ответ ЦБ с курсами на дату: Valute - словарь «код валюты -> данные».
record CbrDailyResponse(
        @JsonProperty("Date") OffsetDateTime date,
        @JsonProperty("Valute") Map<String, CbrValute> valute
) {
}
