package ar.edu.itba.tp1.exchange.business.models;

import java.time.LocalDate;


public record HistoricalConversionResult(
        MoneyAmount originalAmount,
        MoneyAmount convertedAmount,
        ExchangeRate exchangeRate,
        LocalDate date) {

    public HistoricalConversionResult {
        if (originalAmount == null || convertedAmount == null || exchangeRate == null || date == null) {
            throw new IllegalArgumentException(
                    "Original amount, converted amount, exchange rate and date cannot be null");
        }
    }
}