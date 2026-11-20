package io.github.xoage.rates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class CurrencyRatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyRatesApplication.class, args);
	}
}
