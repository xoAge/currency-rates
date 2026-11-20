package io.github.xoage.rates.api;

import io.github.xoage.rates.rate.RatesUnavailableException;
import io.github.xoage.rates.rate.UnknownCurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/// Превращает наши исключения в ответы API в формате ProblemDetail.
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(UnknownCurrencyException.class)
    public ProblemDetail handleUnknownCurrency(UnknownCurrencyException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Валюта не найдена");
        return problem;
    }

    @ExceptionHandler(RatesUnavailableException.class)
    public ProblemDetail handleRatesUnavailable(RatesUnavailableException e) {
        log.warn("Курсы ЦБ недоступны", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Источник курсов временно недоступен, попробуйте позже");
        problem.setTitle("Курсы недоступны");
        return problem;
    }
}
