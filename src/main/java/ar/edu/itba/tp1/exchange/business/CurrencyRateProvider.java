package ar.edu.itba.tp1.exchange.business;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;

public interface CurrencyRateProvider {

    Set<Currency> getSupportedCurrencies();

    ExchangeRate getExchangeRate(Currency fromCurrency, Currency toCurrency);

    List<ExchangeRate> getMultipleExchangeRate(Currency fromCurrency, Set<Currency> toCurrencies);

    List<ExchangeRate> getMultipleExchangeRateOnDate(Currency fromCurrency, Set<Currency> toCurrencies, LocalDate date);
}
