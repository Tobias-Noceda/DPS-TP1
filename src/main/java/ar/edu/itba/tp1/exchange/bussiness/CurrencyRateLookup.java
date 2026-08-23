package ar.edu.itba.tp1.exchange.bussiness;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public class CurrencyRateLookup {

    private final CurrencyRateProvider provider;

    public CurrencyRateLookup(CurrencyRateProvider provider) {
        this.provider = provider;
    }

    public Set<Currency> getSupportedCurrencies() {
        return provider.getSupportedCurrencies();
    }

    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        return provider.getExchangeRate(fromCurrency, toCurrency);
    }
}
