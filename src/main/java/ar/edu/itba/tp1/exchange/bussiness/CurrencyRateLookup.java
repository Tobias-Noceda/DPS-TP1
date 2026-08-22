package ar.edu.itba.tp1.exchange.bussiness;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Currency;

public class CurrencyRateLookup {

    private final static int SCALE = 2;
    private final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final CurrencyRateProvider provider;

    public CurrencyRateLookup(CurrencyRateProvider provider) {
        this.provider = provider;
    }

    public Collection<Currency> getSupportedCurrencies() {
        throw new UnsupportedOperationException("getSupportedCurrencies is not implemented yet");
    }

    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        throw new UnsupportedOperationException("getExchangeRate is not implemented yet");
    }
}
