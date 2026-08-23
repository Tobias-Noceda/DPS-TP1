package ar.edu.itba.tp1.exchange.providers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ar.edu.itba.tp1.exchange.bussiness.CurrencyRateProvider;
import ar.edu.itba.tp1.exchange.providers.http.HttpApiResponse;
import ar.edu.itba.tp1.exchange.providers.http.HttpClient;
import ar.edu.itba.tp1.exchange.providers.http.UnirestHttpClient;

public class FreeCurrencyRateProvider implements CurrencyRateProvider {

	private static final String API_URL = "https://api.freecurrencyapi.com/v1";


	private final HttpClient httpClient;
	private final Map<String, String> authHeaders;

	public FreeCurrencyRateProvider(final String apiKey) {
		this(apiKey, new UnirestHttpClient());
	}

	FreeCurrencyRateProvider(final String apiKey, final HttpClient httpClient) {
		this.httpClient = httpClient;
		this.authHeaders = Map.of(
				"accept", "application/json",
				"apikey", apiKey
		);
	}

	@Override
	public Set<Currency> getSupportedCurrencies() {
		final var response = httpClient.get(API_URL+"/currencies", authHeaders, Map.of());
		return extractSupportedCurrencies(response);
	}

	@Override
	public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
		final var response = httpClient.get(API_URL+"/latest", authHeaders, Map.of(
				"base_currency", fromCurrency.getCurrencyCode(),
				"currencies", toCurrency.getCurrencyCode()
		));
		return extractExchangeRate(response, toCurrency);
	}

	@Override
	public Map<Currency, BigDecimal> getExchangeRates(Currency fromCurrency, Collection<Currency> toCurrencies) {
		throw new UnsupportedOperationException("getExchangeRates is not implemented yet");
	}

	@Override
	public Map<Currency, BigDecimal> getMultipleExchangeRateOnDate(Currency fromCurrency, Collection<Currency> toCurrencies, LocalDate date) {
		throw new UnsupportedOperationException("getMultipleExchangeRateOnDate is not implemented yet");
	}

	private BigDecimal extractExchangeRate(HttpApiResponse response, Currency toCurrency) {
		return new Gson().fromJson(response.body(), ExchangeRateResponse.class).getExchange(toCurrency);
	}

	private Set<Currency> extractSupportedCurrencies(HttpApiResponse response) {
		return new Gson().fromJson(response.body(), CurrenciesResponse.class).getSupportedCurrencies();
	}

	private static class ExchangeRateResponse {
		private Map<String, Double> data;

		public BigDecimal getExchange(final Currency toCurrency) {
			return BigDecimal.valueOf(this.data.get(toCurrency.getCurrencyCode()));
		}
	}

	private static class CurrenciesResponse {
		private Map<String, JsonObject> data;

		public Set<Currency> getSupportedCurrencies() {
			return data.keySet().stream()
					.map(Currency::getInstance)
					.collect(Collectors.toSet());
		}
	}
}
