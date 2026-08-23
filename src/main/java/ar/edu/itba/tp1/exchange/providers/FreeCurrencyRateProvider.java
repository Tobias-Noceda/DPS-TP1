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
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;

import ar.edu.itba.tp1.exchange.bussiness.CurrencyRateProvider;

public class FreeCurrencyRateProvider implements CurrencyRateProvider {

	private static final String LATEST_API_URL = "https://api.freecurrencyapi.com/v1/latest";
	private static final String CURRENCIES_API_URL = "https://api.freecurrencyapi.com/v1/currencies";
	private final GetRequest baseGetRequest;
	private final String apiKey;

	public FreeCurrencyRateProvider(final String apiKey) {
		this.apiKey = apiKey;
		this.baseGetRequest = Unirest.get(LATEST_API_URL)
				.header("accept", "application/json")
				.header("apikey", apiKey);
	}

	@Override
	public Set<Currency> getSupportedCurrencies() {
		final var response = callCurrenciesApi();
		return extractSupportedCurrencies(response);
	}

	@Override
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
		final var response = callApi(fromCurrency, toCurrency);
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

	private HttpResponse<JsonNode> callApi(Currency fromCurrency, Currency toCurrency) {
        try {
            final var response = baseGetRequest
                    .queryString("base_currency", fromCurrency.getCurrencyCode())
					.queryString("currencies", toCurrency.getCurrencyCode())
                    .asJson();
            
            if (response.getStatus() != 200) {
                System.err.println("Error: " + response.getStatus());
                throw new RuntimeException("API call failed with status: " + response.getStatus());
            }
            
            return response;
        } catch (final Exception e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("API call failed", e);
        }
    }

    private BigDecimal extractExchangeRate(HttpResponse<JsonNode> response, Currency toCurrency) {
		return new Gson().fromJson(response.getBody().toString(), ExchangeRateResponse.class).getExchange(toCurrency);
    }

	private HttpResponse<JsonNode> callCurrenciesApi() {
		try {
			final var response = Unirest.get(CURRENCIES_API_URL)
					.header("accept", "application/json")
					.header("apikey", apiKey)
					.asJson();

			if (response.getStatus() != 200) {
				System.err.println("Error: " + response.getStatus());
				throw new RuntimeException("API call failed with status: " + response.getStatus());
			}

			return response;
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
			throw new RuntimeException("API call failed", e);
		}
	}

	private Set<Currency> extractSupportedCurrencies(HttpResponse<JsonNode> response) {
		return new Gson().fromJson(response.getBody().toString(), CurrenciesResponse.class).getSupportedCurrencies();
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
