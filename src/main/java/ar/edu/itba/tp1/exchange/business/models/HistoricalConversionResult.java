package ar.edu.itba.tp1.exchange.business.models;

import java.time.LocalDateTime;

public record HistoricalConversionResult(
        MoneyAmount originalAmount,
        MoneyAmount convertedAmount,
        ExchangeRate exchangeRate,
        LocalDateTime timestamp) {

    public HistoricalConversionResult {
        if (originalAmount == null || convertedAmount == null || exchangeRate == null || timestamp == null) {
            throw new IllegalArgumentException(
                    "Original amount, converted amount, exchange rate and timestamp cannot be null");
        }
    }
}
