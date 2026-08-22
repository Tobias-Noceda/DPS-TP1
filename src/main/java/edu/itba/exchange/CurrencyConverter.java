package edu.itba.class1.exchange;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;

import java.util.Map;

public class CurrencyConverter {

	public double convert(String fromCurrency, String toCurrency, double amount) {
		try {

			// Query the API using API Key, base currency and target currency.
			final var response = Unirest.get("https://api.freecurrencyapi.com/v1/latest")
					.queryString("base_currency", fromCurrency).queryString("currencies", toCurrency)
					.header("accept", "application/json")
					.header("apikey", "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6").asJson();

			// Check if the response is successful (status code 200).
			if (response.getStatus() != 200) {
				System.err.println("Error: " + response.getStatus());
			}

			// Parse the response body to a Java object.
			final var exchangeRateResponse = new Gson().fromJson(response.getBody().toString(),
					ExchangeRateResponse.class);

			// Calculate the exchange rate and return the result.
			return amount * exchangeRateResponse.getExchange(toCurrency);
		} catch (final Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	// Define a nested class to represent the response body.
	private static class ExchangeRateResponse {
		private Map<String, Double> data;

		public void setData(Map<String, Double> data) {
			this.data = data;
		}

		public double getExchange(final String toCurrency) {
			return this.data.get(toCurrency);
		}
	}

}
