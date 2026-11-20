package io.github.xoage.rates.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/// Включает кеширование. Хранилище (Caffeine) Spring Boot настраивает сам по application.properties.
@Configuration
@EnableCaching
public class CacheConfig {

    /// Имя кеша с курсами ЦБ на день.
    public static final String DAILY_RATES = "dailyRates";
}
