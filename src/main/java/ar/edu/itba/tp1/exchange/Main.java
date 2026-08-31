package ar.edu.itba.tp1.exchange;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.itba.tp1.exchange.business.CurrencyConverter;
import ar.edu.itba.tp1.exchange.business.models.HistoricalConversionResult;
import ar.edu.itba.tp1.exchange.business.models.MoneyAmount;
import ar.edu.itba.tp1.exchange.providers.FreeCurrencyRateProvider;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiResponseException;

/**
 * Punto de entrada. Se usa desde la linea de comandos, ver USAGE.
 *
 * Es lo unico que sabe que existe FreeCurrencyRateProvider: arma el grafo de
 * objetos y se lo entrega al negocio.
 */
public class Main {

    private static final String API_KEY_ENV_VARIABLE = "FREECURRENCY_API_KEY";

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

            Requiere la variable de entorno FREECURRENCY_API_KEY.
            """;

    public static void main(String[] args) {
        final var rateProvider = new FreeCurrencyRateProvider(apiKeyFromEnvironment());
        final var converter = new CurrencyConverter(rateProvider, Clock.systemDefaultZone());

        try {
            switch (args.length == 0 ? "" : args[0]) {
                case "currencies" -> printCurrencies(rateProvider.getSupportedCurrencies());
                case "rate" -> printRate(converter, args);
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

    private static String apiKeyFromEnvironment() {
        final var apiKey = System.getenv(API_KEY_ENV_VARIABLE);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + API_KEY_ENV_VARIABLE);
        }
        return apiKey;
    }

    private static void printCurrencies(final Set<Currency> currencies) {
        System.out.println(currencies.stream().map(Currency::getCurrencyCode).sorted().collect(Collectors.joining(", ")));
        System.out.println("(" + currencies.size() + " monedas soportadas)");
    }

    private static void printRate(final CurrencyConverter converter, final String[] args) {
        requireArguments(args, 3);
        final var exchangeRate = converter.getExchangeRate(currency(args[1]), currency(args[2]));

        System.out.println("1 " + exchangeRate.fromCurrency().getCurrencyCode()
                + " = " + exchangeRate.rate().toPlainString()
                + " " + exchangeRate.toCurrency().getCurrencyCode());
    }

    private static void printConversions(final CurrencyConverter converter, final String[] args) {
        requireArguments(args, 4);
        final var fromMoney = new MoneyAmount(amount(args[1]), currency(args[2]));
        final var toCurrencies = currencies(args[3]);

        if (args.length > 4) {
            converter.convertMultipleOnDate(fromMoney, toCurrencies, date(args[4]).atStartOfDay()).stream()
                    .sorted(Comparator.comparing(conversion -> conversion.exchangeRate().toCurrency().getCurrencyCode()))
                    .forEach(Main::printHistoricalConversion);
        } else {
            converter.convertMultiple(fromMoney, toCurrencies).stream()
                    .sorted(Comparator.comparing(conversion -> conversion.exchangeRate().toCurrency().getCurrencyCode()))
                    .forEach(Main::printConversion);
        }
    }

    private static void printConversion(final HistoricalConversionResult conversion) {
        System.out.println("%s -> %s (cotizacion: %s, obtenida el %s)".formatted(
                format(conversion.originalAmount()),
                format(conversion.convertedAmount()),
                conversion.exchangeRate().rate().toPlainString(),
                conversion.timestamp().truncatedTo(ChronoUnit.SECONDS)
        ));
    }

    private static void printHistoricalConversion(final HistoricalConversionResult conversion) {
        System.out.println("%s -> %s (cotizacion: %s, vigente el %s)".formatted(
                format(conversion.originalAmount()),
                format(conversion.convertedAmount()),
                conversion.exchangeRate().rate().toPlainString(),
                conversion.timestamp().toLocalDate()
        ));
    }

    private static String format(final MoneyAmount money) {
        return money.amount().toPlainString() + " " + money.currency().getCurrencyCode();
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
