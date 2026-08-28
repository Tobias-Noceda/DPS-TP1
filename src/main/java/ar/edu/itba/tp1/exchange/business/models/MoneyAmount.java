package ar.edu.itba.tp1.exchange.business.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record MoneyAmount(BigDecimal amount, Currency currency) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public MoneyAmount {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency cannot be null");
        }
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public MoneyAmount convertTo(final ExchangeRate exchangeRate) {
        if (!this.currency.equals(exchangeRate.fromCurrency())) {
            throw new IllegalArgumentException(
                    "Cannot apply a " + exchangeRate.fromCurrency().getCurrencyCode()
                            + " rate to an amount in " + this.currency.getCurrencyCode());
        }
        return new MoneyAmount(this.amount.multiply(exchangeRate.rate()), exchangeRate.toCurrency());
    }
}