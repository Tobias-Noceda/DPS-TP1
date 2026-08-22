package edu.itba.class1.exchange;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

class CurrencyConverterTest {

	@Test
	void testConvert() {
		// Given
		final var converter = new CurrencyConverter();

		// When
		final var result = converter.convert("EUR", "USD", 100);

		// Then
		assertThat(result).isCloseTo(115.28, within(0.01));
	}
}