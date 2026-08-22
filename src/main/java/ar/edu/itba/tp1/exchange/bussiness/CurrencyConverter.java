package ar.edu.itba.tp1.exchange.bussiness;

import lombok.AllArgsConstructor;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Currency;

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

	public Collection<ConvertedMoneyAmount> convertMultiple(MoneyAmount fromMoney, Collection<Currency> toCurrencies) {
		throw new UnsupportedOperationException("convertMultiple is not implemented yet");
	}

	public Collection<ConvertedMoneyAmount> convertMultipleOnDate(MoneyAmount amount, Collection<Currency> toCurrencies, LocalDate date) {
		throw new UnsupportedOperationException("convertMultipleOnDate is not implemented yet");
	}
}
