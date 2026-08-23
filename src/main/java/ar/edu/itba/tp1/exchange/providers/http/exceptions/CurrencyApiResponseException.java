package ar.edu.itba.tp1.exchange.providers.http.exceptions;

public class CurrencyApiResponseException extends CurrencyApiException {

    private final int statusCode;
    private final String responseBody;

    public CurrencyApiResponseException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
