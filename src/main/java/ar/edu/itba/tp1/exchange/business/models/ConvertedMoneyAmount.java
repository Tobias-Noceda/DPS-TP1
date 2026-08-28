package ar.edu.itba.tp1.exchange.business.models;

import java.time.LocalDateTime;

public record ConvertedMoneyAmount(
        MoneyAmount originalAmount,
        MoneyAmount convertedAmount,
        ExchangeRate exchangeRate,
        LocalDateTime timestamp) {

    public ConvertedMoneyAmount {
        if (originalAmount == null || convertedAmount == null || exchangeRate == null || timestamp == null) {
            throw new IllegalArgumentException(
                    "Original amount, converted amount, exchange rate and timestamp cannot be null");
        }
    }
}
