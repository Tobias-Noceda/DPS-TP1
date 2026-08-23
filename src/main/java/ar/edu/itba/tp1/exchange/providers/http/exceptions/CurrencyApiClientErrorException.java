package ar.edu.itba.tp1.exchange.providers.http.exceptions;

public class CurrencyApiClientErrorException extends CurrencyApiResponseException {

    public CurrencyApiClientErrorException(int statusCode, String responseBody) {
        super("Currency exchange API rejected the request with status " + statusCode, statusCode, responseBody);
    }
}
