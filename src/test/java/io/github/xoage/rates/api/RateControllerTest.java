package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.Conversion;
import io.github.xoage.rates.rate.RateService;
import io.github.xoage.rates.rate.RatesUnavailableException;
import io.github.xoage.rates.rate.UnknownCurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateController.class)
class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateService rateService;

    @Test
    @DisplayName("GET /convert - 200 и результат в JSON")
    void convert() throws Exception {
        given(rateService.convert("USD", "EUR", new BigDecimal("100")))
                .willReturn(new Conversion("USD", "EUR", new BigDecimal("100"),
                        new BigDecimal("92.31"), LocalDate.of(2026, 11, 21)));

        mockMvc.perform(get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.result").value(92.31))
                .andExpect(jsonPath("$.date").value("2026-11-21"));
    }

    @Test
    @DisplayName("Неизвестная валюта - 404 в формате ProblemDetail")
    void unknownCurrencyReturns404() throws Exception {
        given(rateService.getRate("XYZ")).willThrow(new UnknownCurrencyException("XYZ"));

        mockMvc.perform(get("/rates/XYZ"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Валюта не найдена"))
                .andExpect(jsonPath("$.detail").value("Неизвестная валюта: XYZ"));
    }

    @Test
    @DisplayName("Код не из трёх букв - 400, сервис не вызывается")
    void invalidCodeReturns400() throws Exception {
        mockMvc.perform(get("/rates/US"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rateService);
    }

    @Test
    @DisplayName("Отрицательная сумма - 400, сервис не вызывается")
    void negativeAmountReturns400() throws Exception {
        mockMvc.perform(get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "-5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rateService);
    }

    @Test
    @DisplayName("ЦБ недоступен - 503 с общей фразой без подробностей")
    void ratesUnavailableReturns503() throws Exception {
        given(rateService.getDailyRates())
                .willThrow(new RatesUnavailableException("Не удалось получить курсы ЦБ",
                        new RuntimeException("Connect timed out")));

        mockMvc.perform(get("/rates"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Курсы недоступны"))
                .andExpect(jsonPath("$.detail").value("Источник курсов временно недоступен, попробуйте позже"));
    }
}