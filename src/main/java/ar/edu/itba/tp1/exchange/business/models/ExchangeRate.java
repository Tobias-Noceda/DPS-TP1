package ar.edu.itba.tp1.exchange.business.models;

import java.math.BigDecimal;
import java.util.Currency;


public record ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {

    public ExchangeRate {
        if (fromCurrency == null || toCurrency == null || rate == null) {
            throw new IllegalArgumentException("From currency, to currency and rate cannot be null");
        }
    }
}