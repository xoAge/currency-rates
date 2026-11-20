package io.github.xoage.rates.rate;

/// Запрошена валюта, которой нет в курсах ЦБ.
public class UnknownCurrencyException extends RuntimeException {

    public UnknownCurrencyException(String code) {
        super("Неизвестная валюта" + code);
    }
}
