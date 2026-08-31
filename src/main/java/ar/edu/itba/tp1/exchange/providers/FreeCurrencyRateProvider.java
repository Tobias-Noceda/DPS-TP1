package ar.edu.itba.tp1.exchange.providers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ar.edu.itba.tp1.exchange.business.CurrencyRateProvider;
import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;
import ar.edu.itba.tp1.exchange.providers.http.HttpApiResponse;
import ar.edu.itba.tp1.exchange.providers.http.HttpClient;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiMissingDataException;

public class FreeCurrencyRateProvider implements CurrencyRateProvider {

	private static final String API_URL = "https://api.freecurrencyapi.com/v1";
	private static final Gson GSON = new Gson();

	private final HttpClient httpClient;
	private final Map<String, String> authHeaders;

	public FreeCurrencyRateProvider(final String apiKey, final HttpClient httpClient) {
		this.httpClient = httpClient;
		this.authHeaders = Map.of(
				"accept", "application/json",
				"apikey", apiKey
		);
	}

	@Override
	public Set<Currency> getSupportedCurrencies() {
		final var response = httpClient.get(API_URL + "/currencies", authHeaders, Map.of());
		return GSON.fromJson(response.body(), CurrenciesResponse.class).getSupportedCurrencies();
	}

	@Override
	public ExchangeRate getExchangeRate(final Currency fromCurrency, final Currency toCurrency) {
		return fetchLatestExchangeRates(fromCurrency, toCurrency.getCurrencyCode()).stream()
				.filter(exchangeRate -> exchangeRate.toCurrency().equals(toCurrency))
				.findFirst()
				.orElseThrow(() -> CurrencyApiMissingDataException.forCurrency(toCurrency));
	}

	@Override
	public List<ExchangeRate> getMultipleExchangeRate(final Currency fromCurrency, final Set<Currency> toCurrencies) {
		final var exchangeRates = fetchLatestExchangeRates(fromCurrency, codesOf(toCurrencies));
		requireRatesFor(toCurrencies, exchangeRates);
		return exchangeRates;
	}

	@Override
	public List<ExchangeRate> getMultipleExchangeRateOnDate(final Currency fromCurrency, final Set<Currency> toCurrencies,
	                                                        final LocalDate date) {
		final var response = httpClient.get(API_URL + "/historical", authHeaders, Map.of(
				"base_currency", fromCurrency.getCurrencyCode(),
				"currencies", codesOf(toCurrencies),
				"date", date.toString()
		));
		final var exchangeRates = toExchangeRates(fromCurrency, ratesOnDateOf(response, date));
		requireRatesFor(toCurrencies, exchangeRates);
		return exchangeRates;
	}

	private List<ExchangeRate> fetchLatestExchangeRates(final Currency fromCurrency, final String currencyCodes) {
		final var response = httpClient.get(API_URL + "/latest", authHeaders, Map.of(
				"base_currency", fromCurrency.getCurrencyCode(),
				"currencies", currencyCodes
		));
		return toExchangeRates(fromCurrency, ratesOf(response));
	}

	private Map<String, BigDecimal> ratesOf(final HttpApiResponse response) {
		return GSON.fromJson(response.body(), ExchangeRateResponse.class).getExchanges();
	}

	private Map<String, BigDecimal> ratesOnDateOf(final HttpApiResponse response, final LocalDate date) {
		return GSON.fromJson(response.body(), HistoricalExchangeRateResponse.class).getExchanges(date);
	}

	private List<ExchangeRate> toExchangeRates(final Currency fromCurrency, final Map<String, BigDecimal> rates) {
		return rates.entrySet().stream()
				.map(rate -> new ExchangeRate(fromCurrency, Currency.getInstance(rate.getKey()), rate.getValue()))
				.toList();
	}

	private void requireRatesFor(final Set<Currency> requestedCurrencies, final List<ExchangeRate> exchangeRates) {
		final var returnedCurrencies = exchangeRates.stream()
				.map(ExchangeRate::toCurrency)
				.collect(Collectors.toSet());
		final var missingCurrencies = requestedCurrencies.stream()
				.filter(currency -> !returnedCurrencies.contains(currency))
				.toList();
		if (!missingCurrencies.isEmpty()) {
			throw CurrencyApiMissingDataException.forCurrencies(missingCurrencies);
		}
	}

	private static String codesOf(final Set<Currency> currencies) {
		return currencies.stream().map(Currency::getCurrencyCode).collect(Collectors.joining(","));
	}

	private static class ExchangeRateResponse {
		private Map<String, BigDecimal> data;

		public Map<String, BigDecimal> getExchanges() {
			return data == null ? Map.of() : data;
		}
	}

	private static class HistoricalExchangeRateResponse {
		private Map<String, Map<String, BigDecimal>> data;

		public Map<String, BigDecimal> getExchanges(final LocalDate date) {
			if (data == null || !data.containsKey(date.toString())) {
				throw CurrencyApiMissingDataException.forDate(date);
			}
			return data.get(date.toString());
		}
	}

	private static class CurrenciesResponse {
		private Map<String, JsonObject> data;

		public Set<Currency> getSupportedCurrencies() {
			if (data == null) {
				throw CurrencyApiMissingDataException.forSupportedCurrencies();
			}
			return data.keySet().stream()
					.map(Currency::getInstance)
					.collect(Collectors.toUnmodifiableSet());
		}
	}
}
