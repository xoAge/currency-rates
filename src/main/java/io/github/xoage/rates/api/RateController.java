package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.DailyRates;
import io.github.xoage.rates.rate.Rate;
import io.github.xoage.rates.rate.RateService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/// REST API курсов валют.
@RestController
public class RateController {

    private static final String CURRENCY_CODE = "[A-Za-z]{3}";

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping("/rates")
    public DailyRatesResponse getRates() {
        return DailyRatesResponse.from(rateService.getDailyRates());
    }

    @GetMapping("/rates/{code}")
    public RateResponse getRate(@PathVariable @Pattern(regexp = CURRENCY_CODE) String code) {
        return RateResponse.from(rateService.getRate(code));
    }

    @GetMapping("/convert")
    public ConversionResponse convert(
            @RequestParam @Pattern(regexp = CURRENCY_CODE) String from,
            @RequestParam @Pattern(regexp = CURRENCY_CODE) String to,
            @RequestParam @Positive BigDecimal amount) {
        return ConversionResponse.from(rateService.convert(from, to, amount));
    }
}
