package ar.edu.itba.tp1.exchange.bussiness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

public interface CurrencyRateProvider {
    Set<Currency> getSupportedCurrencies();
    BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency);
    Map<Currency, BigDecimal> getMultipleExchangeRate(Currency fromCurrency, Set<Currency> toCurrencies);
    Map<Currency, BigDecimal> getMultipleExchangeRateOnDate(Currency fromCurrency, Set<Currency> toCurrencies, LocalDate date);
}
