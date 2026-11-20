package io.github.xoage.rates.cbr;

import io.github.xoage.rates.rate.DailyRates;
import io.github.xoage.rates.rate.RatesUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CbrClientTest {

    private static final String BASE_URL = "https://cbr.test";

    private static final String CBR_JSON = """
            {
              "Date": "2026-11-21T11:30:00+03:00",
              "PreviousDate": "2026-11-20T11:30:00+03:00",
              "Valute": {
                "USD": {"ID": "R01235", "NumCode": "840", "CharCode": "USD", "Nominal": 1,
                        "Name": "Доллар США", "Value": 92.5032, "Previous": 92.1},
                "JPY": {"ID": "R01820", "NumCode": "392", "CharCode": "JPY", "Nominal": 100,
                        "Name": "Японских иен", "Value": 61.2345, "Previous": 61.0}
              }
            }
            """;

    private MockRestServiceServer server;
    private CbrClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        client = new CbrClient(builder.build(), objectMapper);
    }

    @Test
    @DisplayName("Читает курсы, даже если ЦБ отдаёт тип application/javascript")
    void readsRatesWithJavascriptContentType() {
        server.expect(requestTo(BASE_URL + "/daily_json.js"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(CBR_JSON, MediaType.parseMediaType("application/javascript")));

        DailyRates rates = client.fetchDailyRates();

        assertThat(rates.date()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(rates.find("USD").orElseThrow().rubPerUnit()).isEqualByComparingTo("92.5032");
        assertThat(rates.find("JPY").orElseThrow().rubPerUnit()).isEqualByComparingTo("0.612345");
        server.verify();
    }

    @Test
    @DisplayName("Ошибка 500 от ЦБ превращается в RatesUnavailableException")
    void wrapsServerError() {
        server.expect(requestTo(BASE_URL + "/daily_json.js"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchDailyRates())
                .isInstanceOf(RatesUnavailableException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    @DisplayName("Битый JSON превращается в RatesUnavailableException")
    void wrapsBrokenJson() {
        server.expect(requestTo(BASE_URL + "/daily_json.js"))
                .andRespond(withSuccess("это не json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchDailyRates())
                .isInstanceOf(RatesUnavailableException.class)
                .hasCauseInstanceOf(JacksonException.class);
    }
}