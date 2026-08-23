package ar.edu.itba.tp1.exchange.providers.http;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiClientErrorException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiConnectionException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiServerErrorException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnirestHttpClientTest {

	private static final String UNREACHABLE_URL = "http://127.0.0.1:1/currencies";

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance().build();

	private final UnirestHttpClient httpClient = new UnirestHttpClient();

	@Test
	void testGetOnSuccessReturnsStatusAndBodyAndForwardsHeadersAndQueryParams() {
		wireMock.stubFor(get(urlPathEqualTo("/currencies"))
				.withHeader("apikey", equalTo("test-key"))
				.withQueryParam("base_currency", equalTo("USD"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("{\"data\":{}}")));

		final var response = httpClient.get(
				wireMock.baseUrl() + "/currencies",
				Map.of("apikey", "test-key"),
				Map.of("base_currency", "USD")
		);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).isEqualTo("{\"data\":{}}");
	}

	@Test
	void testGetOnClientErrorThrowsCurrencyApiClientErrorExceptionWithStatusAndBody() {
		wireMock.stubFor(get(urlPathEqualTo("/currencies"))
				.willReturn(aResponse().withStatus(404).withBody("{\"message\":\"Not Found\"}")));

		assertThatThrownBy(() -> httpClient.get(wireMock.baseUrl() + "/currencies", Map.of(), Map.of()))
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(CurrencyApiClientErrorException.class))
				.satisfies(exception -> {
					assertThat(exception.getStatusCode()).isEqualTo(404);
					assertThat(exception.getResponseBody()).contains("Not Found");
				});
	}

	@Test
	void testGetOnServerErrorThrowsCurrencyApiServerErrorExceptionWithStatusAndBody() {
		wireMock.stubFor(get(urlPathEqualTo("/currencies"))
				.willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

		assertThatThrownBy(() -> httpClient.get(wireMock.baseUrl() + "/currencies", Map.of(), Map.of()))
				.asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(CurrencyApiServerErrorException.class))
				.satisfies(exception -> {
					assertThat(exception.getStatusCode()).isEqualTo(500);
					assertThat(exception.getResponseBody()).isEqualTo("Internal Server Error");
				});
	}

	@Test
	void testGetOnConnectionFailureThrowsCurrencyApiConnectionException() {
		assertThatThrownBy(() -> httpClient.get(UNREACHABLE_URL, Map.of(), Map.of()))
				.isInstanceOf(CurrencyApiConnectionException.class)
				.hasCauseInstanceOf(com.mashape.unirest.http.exceptions.UnirestException.class);
	}
}
