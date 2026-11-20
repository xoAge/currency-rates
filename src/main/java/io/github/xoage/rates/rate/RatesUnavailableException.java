package io.github.xoage.rates.rate;

/// Источник курсов недоступен или ответил некорректно. В API превращается в 503.
public class RatesUnavailableException extends RuntimeException {

    public RatesUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
