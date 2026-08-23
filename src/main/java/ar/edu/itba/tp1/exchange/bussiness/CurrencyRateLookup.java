package ar.edu.itba.tp1.exchange.bussiness;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Set;

public class CurrencyRateLookup {

    private final static int SCALE = 2;
    private final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final CurrencyRateProvider provider;

    public CurrencyRateLookup(CurrencyRateProvider provider) {
        this.provider = provider;
    }

    public Set<Currency> getSupportedCurrencies() {
        return provider.getSupportedCurrencies();
    }

    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        throw new UnsupportedOperationException("getExchangeRate is not implemented yet");
    }
}
