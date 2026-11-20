package io.github.xoage.rates.rate;

import io.github.xoage.rates.cbr.CbrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    private static final DailyRates RATES = new DailyRates(LocalDate.of(2026, 11, 21), Map.of(
            "RUB", new Rate("RUB", "Российский рубль", BigDecimal.ONE),
            "USD", new Rate("USD", "Доллар США", new BigDecimal("90")),
            "EUR", new Rate("EUR", "Евро", new BigDecimal("100")),
            "JPY", new Rate("JPY", "Японская иена", new BigDecimal("0.6"))
    ));

    @Mock
    private CbrClient cbrClient;

    @InjectMocks
    private RateService rateService;

    @BeforeEach
    void setUp() {
        given(cbrClient.fetchDailyRates()).willReturn(RATES);
    }

    @Test
    @DisplayName("Кросс-курс через рубль: 100 USD × 90 / 100 = 90 EUR")
    void convertsThroughRub() {
        Conversion conversion = rateService.convert("USD", "EUR", new BigDecimal("100"));

        assertThat(conversion.result()).isEqualByComparingTo("90");
    }

    @Test
    @DisplayName("1000 иен = 600 рублей")
    void convertsJpyToRub() {
        Conversion conversion = rateService.convert("JPY", "RUB", new BigDecimal("1000"));

        assertThat(conversion.result()).isEqualByComparingTo("600");
    }

    @Test
    @DisplayName("Коды в любом регистре, в ответе - заглавными, с датой курсов")
    void normalizesCodes() {
        Conversion conversion = rateService.convert("usd", "eur", new BigDecimal("100"));

        assertThat(conversion.from()).isEqualTo("USD");
        assertThat(conversion.to()).isEqualTo("EUR");
        assertThat(conversion.date()).isEqualTo(LocalDate.of(2026, 11, 21));
    }

    @Test
    @DisplayName("Банковское округление: 0.125 -> 0.12, а не 0.13")
    void roundsHalfEven() {
        Conversion conversion = rateService.convert("RUB", "RUB", new BigDecimal("0.125"));

        assertThat(conversion.result()).isEqualByComparingTo("0.12");
    }

    @Test
    @DisplayName("Неизвестная валюта - UnknownCurrencyException")
    void throwsForUnknownCurrency() {
        assertThatThrownBy(() -> rateService.getRate("XYZ"))
                .isInstanceOf(UnknownCurrencyException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    @DisplayName("Конвертация берёт курсы один раз - один снимок на обе валюты")
    void fetchesRatesOnceForConversion() {
        rateService.convert("USD", "EUR", new BigDecimal("100"));

        verify(cbrClient, times(1)).fetchDailyRates();
    }
}