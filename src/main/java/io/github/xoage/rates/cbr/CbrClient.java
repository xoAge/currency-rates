package io.github.xoage.rates.cbr;

import io.github.xoage.rates.config.CbrProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/// Клиент к API курсов ЦБ. Единственное место в приложении, которое знает их формат.
@Component
public class CbrClient {
    private final Logger log = LoggerFactory.getLogger(CbrClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CbrClient(CbrProperties properties, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }

    public CbrDailyResponse fetchDailyRates() {
        log.info("Запрос курсов в API ЦБ");
        String body = restClient.get()
                .uri("/daily_json.js")
                .retrieve()
                .body(String.class);
        return objectMapper.readValue(body,CbrDailyResponse.class);
    }
}
