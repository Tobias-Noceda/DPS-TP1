package ar.edu.itba.tp1.exchange.providers;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.itba.tp1.exchange.providers.http.HttpApiResponse;
import ar.edu.itba.tp1.exchange.providers.http.HttpClient;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiMissingDataException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreeCurrencyRateProviderTest {

	private static final Currency USD = Currency.getInstance("USD");
	private static final Currency EUR = Currency.getInstance("EUR");
	private static final Currency JPY = Currency.getInstance("JPY");
	private static final LocalDate DATE = LocalDate.of(2024, 11, 20);

	@Test
	void testGetSupportedCurrenciesParsesTheCurrencyCodes() {
		final var provider = providerReturning("{\"data\":{\"USD\":{\"code\":\"USD\"},\"EUR\":{\"code\":\"EUR\"}}}");

		final var currencies = provider.getSupportedCurrencies();

		assertThat(currencies).containsExactlyInAnyOrder(USD, EUR);
	}

	@Test
	void testGetExchangeRateParsesTheRequestedRate() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.8543}}");

		final var rate = provider.getExchangeRate(USD, EUR);

		assertThat(rate).isEqualByComparingTo("0.8543");
	}

	@Test
	void testGetMultipleExchangeRateParsesEveryRate() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.8543,\"JPY\":154.2}}");

		final var rates = provider.getMultipleExchangeRate(USD, Set.of(EUR, JPY));

		assertThat(rates).containsOnlyKeys(EUR, JPY);
		assertThat(rates.get(EUR)).isEqualByComparingTo("0.8543");
		assertThat(rates.get(JPY)).isEqualByComparingTo("154.2");
	}

	@Test
	void testGetMultipleExchangeRateOnDateParsesTheRatesOfThatDate() {
		final var provider = providerReturning("{\"data\":{\"2024-11-20\":{\"EUR\":0.9480,\"JPY\":155.27}}}");

		final var rates = provider.getMultipleExchangeRateOnDate(USD, Set.of(EUR, JPY), DATE);

		assertThat(rates).containsOnlyKeys(EUR, JPY);
		assertThat(rates.get(EUR)).isEqualByComparingTo("0.9480");
	}

	@Test
	void testGetExchangeRateThrowsMissingDataWhenTheResponseHasNoRateForTheCurrency() {
		final var provider = providerReturning("{\"data\":{}}");

		assertThatThrownBy(() -> provider.getExchangeRate(USD, EUR))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return a rate for EUR");
	}

	@Test
	void testGetExchangeRateThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(() -> provider.getExchangeRate(USD, EUR))
				.isInstanceOf(CurrencyApiMissingDataException.class);
	}

	@Test
	void testGetMultipleExchangeRateOnDateThrowsMissingDataWhenTheResponseHasNoRatesForThatDate() {
		final var provider = providerReturning("{\"data\":{}}");

		assertThatThrownBy(() -> provider.getMultipleExchangeRateOnDate(USD, Set.of(EUR), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return rates for 2024-11-20");
	}

	@Test
	void testGetMultipleExchangeRateOnDateThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(() -> provider.getMultipleExchangeRateOnDate(USD, Set.of(EUR), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class);
	}

	@Test
	void testGetMultipleExchangeRateThrowsMissingDataWhenTheResponseSkipsOneOfTheRequestedCurrencies() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.8543}}");

		assertThatThrownBy(() -> provider.getMultipleExchangeRate(USD, Set.of(EUR, JPY)))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return rates for JPY");
	}

	@Test
	void testGetMultipleExchangeRateThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(() -> provider.getMultipleExchangeRate(USD, Set.of(EUR, JPY)))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return rates for EUR, JPY");
	}

	@Test
	void testGetMultipleExchangeRateOnDateThrowsMissingDataWhenThatDateSkipsOneOfTheRequestedCurrencies() {
		final var provider = providerReturning("{\"data\":{\"2024-11-20\":{\"EUR\":0.9480}}}");

		assertThatThrownBy(() -> provider.getMultipleExchangeRateOnDate(USD, Set.of(EUR, JPY), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return rates for JPY");
	}

	@Test
	void testGetSupportedCurrenciesThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(provider::getSupportedCurrencies)
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return the list of supported currencies");
	}

	private FreeCurrencyRateProvider providerReturning(final String body) {
		final HttpClient stubHttpClient = (url, headers, queryParams) -> new HttpApiResponse(200, body);
		return new FreeCurrencyRateProvider("test-key", stubHttpClient);
	}
}
