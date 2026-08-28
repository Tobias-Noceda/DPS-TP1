package ar.edu.itba.tp1.exchange.providers;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;
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

		assertThat(provider.getSupportedCurrencies()).containsExactlyInAnyOrder(USD, EUR);
	}

	@Test
	void testGetSupportedCurrenciesThrowsMissingDataWhenTheResponseHasNoData() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(provider::getSupportedCurrencies)
				.isInstanceOf(CurrencyApiMissingDataException.class);
	}

	@Test
	void testGetExchangeRatesReturnsOneExchangeRatePerCurrency() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.8543,\"JPY\":154.2}}");

		final var exchangeRates = provider.getExchangeRates(USD, Set.of(EUR, JPY));

		assertThat(exchangeRates).hasSize(2);
		assertThat(rateFor(exchangeRates, EUR)).isEqualByComparingTo("0.8543");
		assertThat(rateFor(exchangeRates, JPY)).isEqualByComparingTo("154.2");
		assertThat(exchangeRates).allMatch(exchangeRate -> exchangeRate.fromCurrency().equals(USD));
	}

	@Test
	void testGetExchangeRatesDoesNotLosePrecision() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.123456789}}");

		assertThat(rateFor(provider.getExchangeRates(USD, Set.of(EUR)), EUR))
				.isEqualByComparingTo("0.123456789");
	}

	@Test
	void testGetExchangeRatesThrowsMissingDataWhenACurrencyIsNotReturned() {
		final var provider = providerReturning("{\"data\":{\"EUR\":0.8543}}");

		assertThatThrownBy(() -> provider.getExchangeRates(USD, Set.of(EUR, JPY)))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessageContaining("JPY");
	}

	@Test
	void testGetExchangeRatesThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(() -> provider.getExchangeRates(USD, Set.of(EUR)))
				.isInstanceOf(CurrencyApiMissingDataException.class);
	}

	@Test
	void testGetExchangeRatesOnDateReturnsTheRatesOfThatDate() {
		final var provider = providerReturning("{\"data\":{\"2024-11-20\":{\"EUR\":0.9480,\"JPY\":155.27}}}");

		final var exchangeRates = provider.getExchangeRatesOnDate(USD, Set.of(EUR, JPY), DATE);

		assertThat(exchangeRates).hasSize(2);
		assertThat(rateFor(exchangeRates, EUR)).isEqualByComparingTo("0.9480");
	}

	@Test
	void testGetExchangeRatesOnDateThrowsMissingDataWhenThereAreNoRatesForThatDate() {
		final var provider = providerReturning("{\"data\":{}}");

		assertThatThrownBy(() -> provider.getExchangeRatesOnDate(USD, Set.of(EUR), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessage("Currency exchange API did not return rates for 2024-11-20");
	}

	@Test
	void testGetExchangeRatesOnDateThrowsMissingDataWhenTheResponseHasNoDataAtAll() {
		final var provider = providerReturning("{}");

		assertThatThrownBy(() -> provider.getExchangeRatesOnDate(USD, Set.of(EUR), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class);
	}

	@Test
	void testGetExchangeRatesOnDateThrowsMissingDataWhenACurrencyIsNotReturned() {
		final var provider = providerReturning("{\"data\":{\"2024-11-20\":{\"EUR\":0.9480}}}");

		assertThatThrownBy(() -> provider.getExchangeRatesOnDate(USD, Set.of(EUR, JPY), DATE))
				.isInstanceOf(CurrencyApiMissingDataException.class)
				.hasMessageContaining("JPY");
	}

	@Test
	void testTheProviderSendsTheApiKeyAndTheRequestedCurrencies() {
		final var recordingClient = new RecordingHttpClient("{\"data\":{\"EUR\":0.8543}}");
		final var provider = new FreeCurrencyRateProvider("test-key", recordingClient);

		provider.getExchangeRates(USD, Set.of(EUR));

		assertThat(recordingClient.url).endsWith("/latest");
		assertThat(recordingClient.headers).containsEntry("apikey", "test-key");
		assertThat(recordingClient.queryParams).containsEntry("base_currency", "USD");
		assertThat(recordingClient.queryParams).containsEntry("currencies", "EUR");
	}

	@Test
	void testThePublicConstructorBuildsAProviderBackedByUnirest() {
		assertThat(new FreeCurrencyRateProvider("test-key")).isNotNull();
	}

	private static java.math.BigDecimal rateFor(final java.util.List<ExchangeRate> exchangeRates,
	                                            final Currency currency) {
		return exchangeRates.stream()
				.filter(exchangeRate -> exchangeRate.toCurrency().equals(currency))
				.findFirst()
				.orElseThrow()
				.rate();
	}

	private static FreeCurrencyRateProvider providerReturning(final String body) {
		return new FreeCurrencyRateProvider("test-key", new RecordingHttpClient(body));
	}

	/** Fake del puerto HttpClient: devuelve siempre el mismo body y recuerda lo que le pidieron. */
	private static class RecordingHttpClient implements HttpClient {

		private final String body;
		private String url;
		private Map<String, String> headers;
		private Map<String, String> queryParams;

		RecordingHttpClient(final String body) {
			this.body = body;
		}

		@Override
		public HttpApiResponse get(final String url, final Map<String, String> headers,
		                           final Map<String, String> queryParams) {
			this.url = url;
			this.headers = headers;
			this.queryParams = queryParams;
			return new HttpApiResponse(200, this.body);
		}
	}
}
