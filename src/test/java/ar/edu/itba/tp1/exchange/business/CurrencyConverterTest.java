package ar.edu.itba.tp1.exchange.business;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;
import ar.edu.itba.tp1.exchange.business.models.MoneyAmount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyConverterTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");

    private static final Instant NOW = Instant.parse("2026-08-28T10:15:30Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 28);
    private static final LocalDate A_PAST_DATE = LocalDate.of(2024, 11, 20);

    private static final MoneyAmount ONE_HUNDRED_EUROS = new MoneyAmount(new BigDecimal("100"), EUR);
    private static final ExchangeRate EUR_TO_USD = new ExchangeRate(EUR, USD, new BigDecimal("1.1528"));
    private static final ExchangeRate EUR_TO_JPY = new ExchangeRate(EUR, JPY, new BigDecimal("170.5"));

    private CurrencyRateProvider rateProvider;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        this.rateProvider = mock(CurrencyRateProvider.class);
        this.converter = new CurrencyConverter(this.rateProvider, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void testConstructorRequiresARateProvider() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CurrencyConverter(null, Clock.fixed(NOW, ZoneOffset.UTC)));
    }

    @Test
    void testConstructorRequiresAClock() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CurrencyConverter(this.rateProvider, null));
    }

    @Test
    void testGetExchangeRateReturnsTheRateBetweenTwoCurrencies() {
        when(this.rateProvider.getExchangeRates(EUR, Set.of(USD))).thenReturn(List.of(EUR_TO_USD));

        final var exchangeRate = this.converter.getExchangeRate(EUR, USD);

        assertThat(exchangeRate.fromCurrency()).isEqualTo(EUR);
        assertThat(exchangeRate.toCurrency()).isEqualTo(USD);
        assertThat(exchangeRate.rate()).isEqualByComparingTo("1.1528");
    }

    @Test
    void testConvertReturnsTheConvertedAmount() {
        when(this.rateProvider.getExchangeRates(EUR, Set.of(USD))).thenReturn(List.of(EUR_TO_USD));

        final var result = this.converter.convert(ONE_HUNDRED_EUROS, USD);

        assertThat(result.convertedAmount().amount()).isEqualByComparingTo("115.28");
        assertThat(result.convertedAmount().currency()).isEqualTo(USD);
        assertThat(result.originalAmount()).isEqualTo(ONE_HUNDRED_EUROS);
    }

    @Test
    void testConvertCarriesTheExchangeRateThatWasUsed() {
        when(this.rateProvider.getExchangeRates(EUR, Set.of(USD))).thenReturn(List.of(EUR_TO_USD));

        final var result = this.converter.convert(ONE_HUNDRED_EUROS, USD);

        assertThat(result.exchangeRate()).isEqualTo(EUR_TO_USD);
    }

    @Test
    void testConvertCarriesTheMomentTheRateWasRetrieved() {
        when(this.rateProvider.getExchangeRates(EUR, Set.of(USD))).thenReturn(List.of(EUR_TO_USD));

        final var result = this.converter.convert(ONE_HUNDRED_EUROS, USD);

        assertThat(result.timestamp()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }

    @Test
    void testConvertMultipleConvertsToEveryRequestedCurrency() {
        when(this.rateProvider.getExchangeRates(eq(EUR), any()))
                .thenReturn(List.of(EUR_TO_USD, EUR_TO_JPY));

        final var results = this.converter.convertMultiple(ONE_HUNDRED_EUROS, Set.of(USD, JPY));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).convertedAmount().amount()).isEqualByComparingTo("115.28");
        assertThat(results.get(1).convertedAmount().amount()).isEqualByComparingTo("17050.00");
    }

    @Test
    void testConvertMultipleUsesASingleRequest() {
        when(this.rateProvider.getExchangeRates(eq(EUR), any()))
                .thenReturn(List.of(EUR_TO_USD, EUR_TO_JPY));

        this.converter.convertMultiple(ONE_HUNDRED_EUROS, Set.of(USD, JPY));

        verify(this.rateProvider, times(1)).getExchangeRates(any(), any());
    }

    @Test
    void testEveryResultOfTheSameRequestSharesTheSameTimestamp() {
        when(this.rateProvider.getExchangeRates(eq(EUR), any()))
                .thenReturn(List.of(EUR_TO_USD, EUR_TO_JPY));

        final var results = this.converter.convertMultiple(ONE_HUNDRED_EUROS, Set.of(USD, JPY));

        assertThat(results).allMatch(result -> result.timestamp().equals(results.getFirst().timestamp()));
    }

    @Test
    void testConvertMultipleOnDateUsesTheRatesOfThatDate() {
        when(this.rateProvider.getExchangeRatesOnDate(eq(EUR), any(), eq(A_PAST_DATE)))
                .thenReturn(List.of(EUR_TO_USD));

        final var results = this.converter.convertMultipleOnDate(ONE_HUNDRED_EUROS, Set.of(USD), A_PAST_DATE.atStartOfDay());

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().convertedAmount().amount()).isEqualByComparingTo("115.28");
        assertThat(results.getFirst().timestamp()).isEqualTo(A_PAST_DATE.atStartOfDay());
        assertThat(results.getFirst().exchangeRate()).isEqualTo(EUR_TO_USD);
    }

    @Test
    void testTodayIsAValidDateForAHistoricalLookup() {
        when(this.rateProvider.getExchangeRatesOnDate(eq(EUR), any(), eq(TODAY)))
                .thenReturn(List.of(EUR_TO_USD));

        assertThat(this.converter.convertMultipleOnDate(ONE_HUNDRED_EUROS, Set.of(USD), TODAY.atStartOfDay())).hasSize(1);
    }

    @Test
    void testConvertMultipleOnDateRejectsAFutureDateWithoutCallingTheProvider() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> this.converter.convertMultipleOnDate(ONE_HUNDRED_EUROS, Set.of(USD), TODAY.plusDays(1).atStartOfDay()));

        verify(this.rateProvider, never()).getExchangeRatesOnDate(any(), any(), any());
    }

    @Test
    void testConvertMultipleOnDateRequiresADate() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> this.converter.convertMultipleOnDate(ONE_HUNDRED_EUROS, Set.of(USD), null));
    }
}
