package ar.edu.itba.tp1.exchange.providers.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpApiResponseTest {

	@Test
	void testSuccessfulStatusIsNeitherClientNorServerError() {
		final var response = new HttpApiResponse(200, "{}");

		assertThat(response.isClientError()).isFalse();
		assertThat(response.isServerError()).isFalse();
	}

	@Test
	void testRedirectStatusIsNeitherClientNorServerError() {
		final var response = new HttpApiResponse(399, "{}");

		assertThat(response.isClientError()).isFalse();
		assertThat(response.isServerError()).isFalse();
	}

	@Test
	void testLowerBoundaryOfClientErrorIsClientError() {
		final var response = new HttpApiResponse(400, "{}");

		assertThat(response.isClientError()).isTrue();
		assertThat(response.isServerError()).isFalse();
	}

	@Test
	void testUpperBoundaryOfClientErrorIsStillClientError() {
		final var response = new HttpApiResponse(499, "{}");

		assertThat(response.isClientError()).isTrue();
		assertThat(response.isServerError()).isFalse();
	}

	@Test
	void testLowerBoundaryOfServerErrorIsServerError() {
		final var response = new HttpApiResponse(500, "{}");

		assertThat(response.isClientError()).isFalse();
		assertThat(response.isServerError()).isTrue();
	}
}
