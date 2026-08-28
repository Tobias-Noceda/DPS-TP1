package ar.edu.itba.tp1.exchange.business;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import ar.edu.itba.tp1.exchange.business.models.ConvertedMoneyAmount;
import ar.edu.itba.tp1.exchange.business.models.ExchangeRate;
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

	/** Requerimiento 1: listar las monedas soportadas. */
	public Set<Currency> getSupportedCurrencies() {
		return Set.copyOf(this.rateProvider.getSupportedCurrencies());
	}

	/** Requerimiento 3: solo la cotizacion, sin convertir ningun monto. */
	public ExchangeRate getExchangeRate(final Currency fromCurrency, final Currency toCurrency) {
		return this.rateProvider.getExchangeRates(fromCurrency, Set.of(toCurrency)).getFirst();
	}

	/** Requerimientos 2 y 7: conversion simple, con cotizacion y timestamp. */
	public ConvertedMoneyAmount convert(final MoneyAmount fromMoney, final Currency toCurrency) {
		return this.convertMultiple(fromMoney, Set.of(toCurrency)).getFirst();
	}

	/** Requerimiento 5: convertir a varias monedas con un solo request. */
	public List<ConvertedMoneyAmount> convertMultiple(final MoneyAmount fromMoney, final Set<Currency> toCurrencies) {
		final var timestamp = LocalDateTime.now(this.clock);
		return this.rateProvider.getExchangeRates(fromMoney.currency(), toCurrencies).stream()
				.map(exchangeRate -> new ConvertedMoneyAmount(
						fromMoney,
						fromMoney.convertTo(exchangeRate),
						exchangeRate,
						timestamp))
				.toList();
	}

	/** Requerimiento 6: cotizaciones de una fecha pasada. */
	public List<HistoricalConversionResult> convertMultipleOnDate(final MoneyAmount fromMoney,
	                                                              final Set<Currency> toCurrencies, final LocalDate date) {
		this.requireNotInTheFuture(date);
		return this.rateProvider.getExchangeRatesOnDate(fromMoney.currency(), toCurrencies, date).stream()
				.map(exchangeRate -> new HistoricalConversionResult(
						fromMoney,
						fromMoney.convertTo(exchangeRate),
						exchangeRate,
						date))
				.toList();
	}

	private void requireNotInTheFuture(final LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}
		if (date.isAfter(LocalDate.now(this.clock))) {
			throw new IllegalArgumentException("Cannot look up an exchange rate for a future date: " + date);
		}
	}
}