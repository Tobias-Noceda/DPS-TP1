package ar.edu.itba.tp1.exchange.providers.http;

public record HttpApiResponse(int statusCode, String body) {

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
