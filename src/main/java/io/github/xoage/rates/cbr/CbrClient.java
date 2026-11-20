package io.github.xoage.rates.cbr;

import io.github.xoage.rates.config.CacheConfig;
import io.github.xoage.rates.rate.DailyRates;
import io.github.xoage.rates.rate.RatesUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/// Клиент к API курсов ЦБ. Единственное место в приложении, которое знает их формат.
@Component
public class CbrClient {
    private final Logger log = LoggerFactory.getLogger(CbrClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CbrClient(RestClient cbrRestClient, ObjectMapper objectMapper) {
        this.restClient = cbrRestClient;
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = CacheConfig.DAILY_RATES, sync = true)
    public DailyRates fetchDailyRates() {
        log.info("Запрос курсов в API ЦБ");
        try {
            String body = restClient.get()
                    .uri("/daily_json.js")
                    .retrieve()
                    .body(String.class);
            CbrDailyResponse response = objectMapper.readValue(body, CbrDailyResponse.class);
            return CbrMapper.toDailyRates(response);
        } catch (RestClientException | JacksonException e) {
            throw new RatesUnavailableException("Не удалось получить курсы ЦБ", e);
        }
    }
}
