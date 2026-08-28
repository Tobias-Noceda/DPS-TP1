package ar.edu.itba.tp1.exchange.business.models;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ExchangeRateTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void testKeepsBothCurrenciesAndTheRate() {
        final var exchangeRate = new ExchangeRate(EUR, USD, new BigDecimal("1.1528"));

        assertThat(exchangeRate.fromCurrency()).isEqualTo(EUR);
        assertThat(exchangeRate.toCurrency()).isEqualTo(USD);
        assertThat(exchangeRate.rate()).isEqualByComparingTo("1.1528");
    }

    @Test
    void testFromCurrencyCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ExchangeRate(null, USD, BigDecimal.ONE));
    }

    @Test
    void testToCurrencyCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ExchangeRate(EUR, null, BigDecimal.ONE));
    }

    @Test
    void testRateCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ExchangeRate(EUR, USD, null));
    }
}