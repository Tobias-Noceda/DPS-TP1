package ar.edu.itba.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.itba.tp1.exchange.bussiness.CurrencyConverter;
import ar.edu.itba.tp1.exchange.bussiness.CurrencyRateLookup;
import ar.edu.itba.tp1.exchange.bussiness.CurrencyRateProvider;
import ar.edu.itba.tp1.exchange.bussiness.models.MoneyAmount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class CurrencyConverterTest {

	private static final Set<Currency> SUPPORTED_CURRENCIES = Set.of(Currency.getInstance("EUR"), Currency.getInstance("USD"), Currency.getInstance("JPY"));
	private static final CurrencyRateProvider MOCKED_EXCHANGE_PROVIDER = new CurrencyRateProvider() {

		@Override
		public Set<Currency> getSupportedCurrencies() {
			return SUPPORTED_CURRENCIES;
		}

		@Override
		public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
			if (fromCurrency.getCurrencyCode().equals("EUR") && toCurrency.getCurrencyCode().equals("USD")) {
				return BigDecimal.valueOf(1.1528);
			}
			return BigDecimal.ONE; // Default mock behavior
		}

		@Override
		public Map<Currency, BigDecimal> getMultipleExchangeRate(Currency fromCurrency, Set<Currency> toCurrencies) {
			return SUPPORTED_CURRENCIES.stream()
					.filter(toCurrencies::contains)
					.collect(Collectors.toMap(
							toCurrency -> toCurrency,
							toCurrency -> getExchangeRate(fromCurrency, toCurrency)
					));
		}

		@Override
		public Map<Currency, BigDecimal> getMultipleExchangeRateOnDate(Currency fromCurrency, Set<Currency> toCurrencies, LocalDate date) {
			return SUPPORTED_CURRENCIES.stream()
					.filter(toCurrencies::contains)
					.collect(Collectors.toMap(
							toCurrency -> toCurrency,
							toCurrency -> getExchangeRate(fromCurrency, toCurrency)
					));
		}
	};

	private CurrencyConverter converter;
	private CurrencyRateLookup lookup;

	@BeforeEach
	void setUp() {
		converter = new CurrencyConverter(MOCKED_EXCHANGE_PROVIDER);
		lookup = new CurrencyRateLookup(MOCKED_EXCHANGE_PROVIDER);
	}

	@Test
	void testConvert() {
		// Given
		final var fromMoney = new MoneyAmount(BigDecimal.valueOf(100), Currency.getInstance("EUR"));
		final var toCurrency = Currency.getInstance("USD");

		// When
		final var result = converter.convert(fromMoney, toCurrency);

		// Then
		assertThat(result.convertedAmount().amount()).isEqualTo(BigDecimal.valueOf(115.28));
	}

	@Test
	void testConvertMultiple() {
		// Given
		final var fromMoney = new MoneyAmount(BigDecimal.valueOf(100), Currency.getInstance("EUR"));
		final var toCurrencies = Set.of(Currency.getInstance("USD"), Currency.getInstance("JPY"));

		// When
		final var results = converter.convertMultiple(fromMoney, toCurrencies);

		// Then
		assertThat(results).hasSize(2);
		assertThat(results.stream().anyMatch(result -> result.convertedAmount().currency().equals(Currency.getInstance("USD")) && result.convertedAmount().amount().equals(BigDecimal.valueOf(115.28)))).isTrue();
		assertThat(results.stream().anyMatch(result -> result.convertedAmount().currency().equals(Currency.getInstance("JPY")) && result.convertedAmount().amount().equals(new BigDecimal("100.00")))).isTrue();
	}

	@Test
	void testConvertMultipleOnDate() {
		// Given
		final var fromMoney = new MoneyAmount(BigDecimal.valueOf(100), Currency.getInstance("EUR"));
		final var toCurrencies = Set.of(Currency.getInstance("USD"), Currency.getInstance("JPY"));
		final var date = LocalDate.of(2023, 1, 1);

		// When
		final var results = converter.convertMultipleOnDate(fromMoney, toCurrencies, date);

		// Then
		assertThat(results).hasSize(2);
		assertThat(results.stream().anyMatch(result -> result.convertedAmount().currency().equals(Currency.getInstance("USD")) && result.convertedAmount().amount().equals(BigDecimal.valueOf(115.28)))).isTrue();
		assertThat(results.stream().anyMatch(result -> result.convertedAmount().currency().equals(Currency.getInstance("JPY")) && result.convertedAmount().amount().equals(new BigDecimal("100.00")))).isTrue();
		assertThat(results.stream().allMatch(result -> result.timestamp().toLocalDate().equals(date))).isTrue();
	}

	@Test
	void testGetSupportedCurrencies() {
		// When
		final var supportedCurrencies = lookup.getSupportedCurrencies();

		// Then
		assertThat(supportedCurrencies).containsExactlyInAnyOrderElementsOf(SUPPORTED_CURRENCIES);
	}

	@Test
	void testGetExchangeRate() {
		// Given
		final var fromCurrency = Currency.getInstance("EUR");
		final var toCurrency = Currency.getInstance("USD");

		// When
		final var exchangeRate = lookup.getExchangeRate(fromCurrency, toCurrency);

		// Then
		assertThat(exchangeRate).isEqualTo(BigDecimal.valueOf(1.1528));
	}
}