package io.github.xoage.rates.rate;

import io.github.xoage.rates.cbr.CbrClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/// Курсы валют и конвертация. Все суммы считаются через рубль.
@Service
public class RateService {
    /// Денежный результат - 2 знака после запятой (копейки, центы).
    private static final int MONEY_SCALE = 2;

    private final CbrClient cbrClient;

    public RateService(CbrClient cbrClient) {
        this.cbrClient = cbrClient;
    }

    public DailyRates getDailyRates() {
        return cbrClient.fetchDailyRates();
    }

    public Rate getRate(String code) {
        return findRate(getDailyRates(),code);
    }

    public Conversion convert(String from, String to, BigDecimal amount) {
        DailyRates rates = getDailyRates();
        Rate fromRate = findRate(rates,from);
        Rate toRate = findRate(rates,to);

        BigDecimal result = amount
                .multiply(fromRate.rubPerUnit())
                .divide(toRate.rubPerUnit(), MONEY_SCALE, RoundingMode.HALF_EVEN);

        return new Conversion(fromRate.code(), toRate.code(), amount, result, rates.date());
    }

    private static Rate findRate(DailyRates rates, String code) {
        return rates.find(code.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new UnknownCurrencyException(code));
    }
}

