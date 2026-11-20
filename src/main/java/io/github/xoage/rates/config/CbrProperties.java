package io.github.xoage.rates.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

/// Настройки подключения к API курсов ЦБ (префикс cbr.* в application.properties).
@ConfigurationProperties(prefix = "cbr")
@Validated
public record CbrProperties(
        @NotNull URI baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout
        ) {
}
