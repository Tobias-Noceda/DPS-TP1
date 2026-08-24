package ar.edu.itba.tp1.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.itba.tp1.exchange.bussiness.CurrencyConverter;
import ar.edu.itba.tp1.exchange.bussiness.CurrencyRateLookup;
import ar.edu.itba.tp1.exchange.bussiness.models.ConvertedMoneyAmount;
import ar.edu.itba.tp1.exchange.bussiness.models.MoneyAmount;
import ar.edu.itba.tp1.exchange.providers.FreeCurrencyRateProvider;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiResponseException;

/**
 * Punto de entrada. Se usa desde la linea de comandos, ver USAGE.
 */
public class Main {

    private static final String API_KEY = "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6";

    private static final String USAGE = """
            Uso:
              currencies                                     lista las monedas soportadas
              rate <origen> <destino>                        cotizacion entre dos monedas
              convert <monto> <origen> <destinos> [fecha]    convierte a una o varias monedas (fecha: yyyy-MM-dd)

            Ejemplos (ojo: las comillas envuelven todo el -D):
              mvn compile exec:java "-Dexec.args=currencies"
              mvn compile exec:java "-Dexec.args=rate USD EUR"
              mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY"
              mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY 2024-11-20"
            """;

    public static void main(String[] args) {
        final var rateProvider = new FreeCurrencyRateProvider(API_KEY);
        final var converter = new CurrencyConverter(rateProvider);
        final var lookup = new CurrencyRateLookup(rateProvider);

        try {
            switch (args.length == 0 ? "" : args[0]) {
                case "currencies" -> printCurrencies(lookup.getSupportedCurrencies());
                case "rate" -> printRate(lookup, args);
                case "convert" -> printConversions(converter, args);
                default -> System.out.print(USAGE);
            }
        } catch (final CurrencyApiResponseException e) {
            System.out.println("La API respondio con error " + e.getStatusCode() + ": " + e.getResponseBody());
        } catch (final CurrencyApiException e) {
            System.out.println("Error consultando la API: " + e.getMessage());
        } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.print(USAGE);
        }
    }

    private static void printCurrencies(final Set<Currency> currencies) {
        System.out.println(currencies.stream().map(Currency::getCurrencyCode).sorted().collect(Collectors.joining(", ")));
        System.out.println("(" + currencies.size() + " monedas soportadas)");
    }

    private static void printRate(final CurrencyRateLookup lookup, final String[] args) {
        requireArguments(args, 3);
        final var fromCurrency = currency(args[1]);
        final var toCurrency = currency(args[2]);

        final var rate = lookup.getExchangeRate(fromCurrency, toCurrency);

        System.out.println("1 " + fromCurrency.getCurrencyCode() + " = " + rate.toPlainString() + " " + toCurrency.getCurrencyCode());
    }

    private static void printConversions(final CurrencyConverter converter, final String[] args) {
        requireArguments(args, 4);
        final var fromMoney = new MoneyAmount(amount(args[1]), currency(args[2]));
        final var toCurrencies = currencies(args[3]);

        final Collection<ConvertedMoneyAmount> conversions = args.length > 4
                ? converter.convertMultipleOnDate(fromMoney, toCurrencies, date(args[4]))
                : converter.convertMultiple(fromMoney, toCurrencies);

        conversions.stream()
                .sorted(Comparator.comparing(conversion -> conversion.convertedAmount().currency().getCurrencyCode()))
                .forEach(Main::printConversion);
    }

    private static void printConversion(final ConvertedMoneyAmount conversion) {
        System.out.println("%s %s -> %s %s (cotizacion: %s, obtenida el %s)".formatted(
                conversion.originalAmount().amount().toPlainString(),
                conversion.originalAmount().currency().getCurrencyCode(),
                conversion.convertedAmount().amount().toPlainString(),
                conversion.convertedAmount().currency().getCurrencyCode(),
                conversion.rate().toPlainString(),
                conversion.timestamp().truncatedTo(ChronoUnit.SECONDS)
        ));
    }

    private static void requireArguments(final String[] args, final int expected) {
        if (args.length < expected) {
            throw new IllegalArgumentException("Faltan argumentos para '" + args[0] + "'.");
        }
    }

    private static BigDecimal amount(final String value) {
        try {
            return new BigDecimal(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("'" + value + "' no es un monto valido.");
        }
    }

    private static Currency currency(final String code) {
        try {
            return Currency.getInstance(code.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("'" + code + "' no es un codigo de moneda ISO valido.");
        }
    }

    private static Set<Currency> currencies(final String codes) {
        return Arrays.stream(codes.split(",")).map(Main::currency).collect(Collectors.toSet());
    }

    private static LocalDate date(final String value) {
        try {
            return LocalDate.parse(value);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("'" + value + "' no es una fecha valida, se espera yyyy-MM-dd.");
        }
    }
}
