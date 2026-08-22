package ar.edu.itba.tp1.exchange.bussiness.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConvertedMoneyAmount(MoneyAmount originalAmount, MoneyAmount convertedAmount, BigDecimal rate, LocalDateTime timestamp) {
    
    public ConvertedMoneyAmount {
        if (originalAmount == null || convertedAmount == null || timestamp == null || rate == null) {
            throw new IllegalArgumentException("Original amount, converted amount, rate, and timestamp cannot be null");
        }
    }
}
