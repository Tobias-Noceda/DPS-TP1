package ar.edu.itba.tp1.exchange.business.models;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MoneyAmountTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void testAmountIsRoundedToTwoDecimals() {
        final var money = new MoneyAmount(new BigDecimal("10.129"), USD);

        assertThat(money.amount()).isEqualByComparingTo("10.13");
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void testAmountCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new MoneyAmount(null, USD));
    }

    @Test
    void testCurrencyCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new MoneyAmount(BigDecimal.ONE, null));
    }

    @Test
    void testConvertToAppliesTheRateAndChangesTheCurrency() {
        final var money = new MoneyAmount(new BigDecimal("100"), EUR);

        final var converted = money.convertTo(new ExchangeRate(EUR, USD, new BigDecimal("1.1528")));

        assertThat(converted.currency()).isEqualTo(USD);
        assertThat(converted.amount()).isEqualByComparingTo("115.28");
    }

    @Test
    void testConvertToRejectsARateOfAnotherCurrency() {
        final var money = new MoneyAmount(new BigDecimal("100"), EUR);
        final var wrongRate = new ExchangeRate(USD, EUR, BigDecimal.ONE);

        assertThatIllegalArgumentException().isThrownBy(() -> money.convertTo(wrongRate));
    }
}
