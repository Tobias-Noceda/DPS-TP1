package ar.edu.itba.tp1.exchange.business.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HistoricalConversionResultTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final MoneyAmount ONE_EURO = new MoneyAmount(BigDecimal.ONE, EUR);
    private static final MoneyAmount ONE_DOLLAR = new MoneyAmount(BigDecimal.ONE, USD);
    private static final ExchangeRate RATE = new ExchangeRate(EUR, USD, BigDecimal.ONE);
    private static final LocalDate DATE = LocalDate.of(2024, 11, 20);

    @Test
    void testKeepsEveryFieldItWasBuiltWith() {
        final var result = new HistoricalConversionResult(ONE_EURO, ONE_DOLLAR, RATE, DATE);

        assertThat(result.originalAmount()).isEqualTo(ONE_EURO);
        assertThat(result.convertedAmount()).isEqualTo(ONE_DOLLAR);
        assertThat(result.exchangeRate()).isEqualTo(RATE);
        assertThat(result.date()).isEqualTo(DATE);
    }

    @Test
    void testOriginalAmountCannotBeNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalConversionResult(null, ONE_DOLLAR, RATE, DATE));
    }

    @Test
    void testConvertedAmountCannotBeNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalConversionResult(ONE_EURO, null, RATE, DATE));
    }

    @Test
    void testExchangeRateCannotBeNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalConversionResult(ONE_EURO, ONE_DOLLAR, null, DATE));
    }

    @Test
    void testDateCannotBeNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalConversionResult(ONE_EURO, ONE_DOLLAR, RATE, null));
    }
}
