package ar.edu.itba.tp1.exchange.business;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import ar.edu.itba.tp1.exchange.business.models.HistoricalConversionResult;
import ar.edu.itba.tp1.exchange.business.models.MoneyAmount;

public class CurrencyConverter {

	private final CurrencyRateProvider rateProvider;
	private final Clock clock;

	public CurrencyConverter(final CurrencyRateProvider rateProvider, final Clock clock) {
		if (rateProvider == null || clock == null) {
			throw new IllegalArgumentException("Rate provider and clock cannot be null");
		}
		this.rateProvider = rateProvider;
		this.clock = clock;
	}

	public HistoricalConversionResult convert(final MoneyAmount fromMoney, final Currency toCurrency) {
		return this.convertMultiple(fromMoney, Set.of(toCurrency)).getFirst();
	}

	public List<HistoricalConversionResult> convertMultiple(final MoneyAmount fromMoney, final Set<Currency> toCurrencies) {
		final var timestamp = LocalDateTime.now(this.clock);
		return this.rateProvider.getMultipleExchangeRate(fromMoney.currency(), toCurrencies).stream()
				.map(exchangeRate -> new HistoricalConversionResult(
						fromMoney,
						fromMoney.convertTo(exchangeRate),
						exchangeRate,
						timestamp))
				.toList();
	}

	public List<HistoricalConversionResult> convertMultipleOnDate(final MoneyAmount fromMoney,
	                                                              final Set<Currency> toCurrencies, final LocalDateTime dateTime) {
		this.requireNotInTheFuture(dateTime);
		return this.rateProvider.getMultipleExchangeRateOnDate(fromMoney.currency(), toCurrencies, dateTime.toLocalDate()).stream()
				.map(exchangeRate -> new HistoricalConversionResult(
						fromMoney,
						fromMoney.convertTo(exchangeRate),
						exchangeRate,
						dateTime))
				.toList();
	}

	private void requireNotInTheFuture(final LocalDateTime dateTime) {
		if (dateTime == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}
		if (dateTime.isAfter(LocalDateTime.now(this.clock))) {
			throw new IllegalArgumentException("Cannot look up an exchange rate for a future date: " + dateTime);
		}
	}
}