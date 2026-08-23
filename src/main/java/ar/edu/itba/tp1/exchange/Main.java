package ar.edu.itba.tp1.exchange;

import java.math.BigDecimal;
import java.util.Currency;

import ar.edu.itba.tp1.exchange.bussiness.CurrencyConverter;
import ar.edu.itba.tp1.exchange.bussiness.models.MoneyAmount;
import ar.edu.itba.tp1.exchange.providers.FreeCurrencyRateProvider;

public class Main {
    
    public static void main(String[] args) {
        final var currencyExchange = new FreeCurrencyRateProvider("fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6");
        final var converter = new CurrencyConverter(currencyExchange);

        final var money = new MoneyAmount(BigDecimal.valueOf(100), Currency.getInstance("EUR"));
        final var toCurrency = Currency.getInstance("USD");

        final var result = converter.convert(money, toCurrency);
        System.out.println("Converted amount: " + result.convertedAmount().amount() + " " + toCurrency.getCurrencyCode() + " using rate: " + result.rate());
    }
}
