package ar.edu.itba.tp1.exchange.bussiness.models;

import java.math.BigDecimal;
import java.util.Currency;

public record MoneyAmount(BigDecimal amount, Currency currency) {
    public MoneyAmount {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency cannot be null");
        }
    }

    public MoneyAmount add(MoneyAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add MoneyAmount with different currencies");
        }
        return new MoneyAmount(this.amount.add(other.amount), this.currency);
    }

    public MoneyAmount multiply(BigDecimal multiplier) {
        return new MoneyAmount(this.amount.multiply(multiplier), this.currency);
    }
}
