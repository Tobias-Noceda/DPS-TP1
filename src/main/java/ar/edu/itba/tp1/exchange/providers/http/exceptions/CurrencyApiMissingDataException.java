package ar.edu.itba.tp1.exchange.providers.http.exceptions;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.stream.Collectors;

public class CurrencyApiMissingDataException extends CurrencyApiException {

    private CurrencyApiMissingDataException(String missingData) {
        super("Currency exchange API did not return " + missingData);
    }

    public static CurrencyApiMissingDataException forCurrency(Currency currency) {
        return new CurrencyApiMissingDataException("a rate for " + currency.getCurrencyCode());
    }

    public static CurrencyApiMissingDataException forCurrencies(Collection<Currency> currencies) {
        return new CurrencyApiMissingDataException("rates for " + codesOf(currencies));
    }

    public static CurrencyApiMissingDataException forDate(LocalDate date) {
        return new CurrencyApiMissingDataException("rates for " + date);
    }

    public static CurrencyApiMissingDataException forSupportedCurrencies() {
        return new CurrencyApiMissingDataException("the list of supported currencies");
    }

    private static String codesOf(Collection<Currency> currencies) {
        return currencies.stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
