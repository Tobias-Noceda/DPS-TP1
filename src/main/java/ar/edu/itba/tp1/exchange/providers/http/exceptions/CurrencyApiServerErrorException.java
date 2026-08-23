package ar.edu.itba.tp1.exchange.providers.http.exceptions;

public class CurrencyApiServerErrorException extends CurrencyApiResponseException {

    public CurrencyApiServerErrorException(int statusCode, String responseBody) {
        super("Currency exchange API failed with status " + statusCode, statusCode, responseBody);
    }
}
