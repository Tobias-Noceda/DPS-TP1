package ar.edu.itba.tp1.exchange.business;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;

public interface CurrencyRateProvider {

    Set<Currency> getSupportedCurrencies();

    List<ExchangeRate> getExchangeRates(Currency fromCurrency, Set<Currency> toCurrencies);

    List<ExchangeRate> getExchangeRatesOnDate(Currency fromCurrency, Set<Currency> toCurrencies, LocalDate date);
}
