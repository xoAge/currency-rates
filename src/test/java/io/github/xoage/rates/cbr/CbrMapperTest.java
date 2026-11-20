package io.github.xoage.rates.cbr;

import io.github.xoage.rates.rate.DailyRates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CbrMapperTest {

    private static final CbrDailyResponse RESPONSE = new CbrDailyResponse(
            OffsetDateTime.parse("2026-11-21T11:30:00+03:00"),
            Map.of(
                    "USD", new CbrValute("USD", 1, "Доллар США", new BigDecimal("92.5032")),
                    "JPY", new CbrValute("JPY", 100, "Японских иен", new BigDecimal("61.2345"))
            )
    );

    private final DailyRates rates = CbrMapper.toDailyRates(RESPONSE);

    @Test
    @DisplayName("Курс делится на номинал: 100 иен = 61.2345 ₽, значит 1 иена = 0.612345 ₽")
    void dividesValueByNominal() {
        assertThat(rates.find("JPY").orElseThrow().rubPerUnit()).isEqualByComparingTo("0.612345");
    }

    @Test
    @DisplayName("При номинале 1 курс не меняется")
    void keepsValueForNominalOne() {
        assertThat(rates.find("USD").orElseThrow().rubPerUnit()).isEqualByComparingTo("92.5032");
    }

    @Test
    @DisplayName("Добавляется рубль с курсом 1")
    void addsRub() {
        assertThat(rates.find("RUB").orElseThrow().rubPerUnit()).isEqualByComparingTo("1");
    }

    @Test
    @DisplayName("Дата берётся из ответа ЦБ")
    void takesDateFromResponse() {
        assertThat(rates.date()).isEqualTo(LocalDate.of(2026, 11, 21));
    }
}
