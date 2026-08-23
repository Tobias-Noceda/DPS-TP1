package ar.edu.itba.tp1.exchange.bussiness;

import lombok.AllArgsConstructor;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Currency;
import java.util.Set;

import ar.edu.itba.tp1.exchange.bussiness.models.ConvertedMoneyAmount;
import ar.edu.itba.tp1.exchange.bussiness.models.MoneyAmount;

@AllArgsConstructor
public class CurrencyConverter {

	private final static int SCALE = 2;
	private final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
	
	private final CurrencyRateProvider currencyRate;

	public ConvertedMoneyAmount convert(MoneyAmount fromMoney, Currency toCurrency) {
		final var exchangeRate = currencyRate.getExchangeRate(fromMoney.currency(), toCurrency);
		return new ConvertedMoneyAmount(
				fromMoney,
				new MoneyAmount(fromMoney.amount().multiply(exchangeRate).setScale(SCALE, ROUNDING_MODE), toCurrency),
				exchangeRate,
				LocalDateTime.now()
		);
	}

	public Collection<ConvertedMoneyAmount> convertMultiple(MoneyAmount fromMoney, Set<Currency> toCurrencies) {
		final var exchangeRates = currencyRate.getMultipleExchangeRate(fromMoney.currency(), toCurrencies);
		final var timestamp = LocalDateTime.now();

		return exchangeRates.entrySet().stream()
				.map(exchangeRate -> new ConvertedMoneyAmount(
						fromMoney,
						new MoneyAmount(fromMoney.amount().multiply(exchangeRate.getValue()).setScale(SCALE, ROUNDING_MODE), exchangeRate.getKey()),
						exchangeRate.getValue(),
						timestamp
				))
				.toList();
	}

	public Collection<ConvertedMoneyAmount> convertMultipleOnDate(MoneyAmount fromMoney, Set<Currency> toCurrencies, LocalDate date) {
		final var exchangeRates = currencyRate.getMultipleExchangeRateOnDate(fromMoney.currency(), toCurrencies, date);
		final var timestamp = date.atStartOfDay();

		return exchangeRates.entrySet().stream()
				.map(exchangeRate -> new ConvertedMoneyAmount(
						fromMoney,
						new MoneyAmount(fromMoney.amount().multiply(exchangeRate.getValue()).setScale(SCALE, ROUNDING_MODE), exchangeRate.getKey()),
						exchangeRate.getValue(),
						timestamp
				))
				.toList();
	}
}
