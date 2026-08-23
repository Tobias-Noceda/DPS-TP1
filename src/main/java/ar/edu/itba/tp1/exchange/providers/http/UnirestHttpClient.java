package ar.edu.itba.tp1.exchange.providers.http;

import java.util.Map;

import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiClientErrorException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiConnectionException;
import ar.edu.itba.tp1.exchange.providers.http.exceptions.CurrencyApiServerErrorException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UnirestHttpClient implements HttpClient {

    @Override
    public HttpApiResponse get(final String url, final Map<String, String> headers, final Map<String, String> queryParams) {
        try {
            final var request = Unirest.get(url).headers(headers);
            queryParams.forEach(request::queryString);

            final var response = request.asString();
            final var apiResponse = new HttpApiResponse(response.getStatus(), response.getBody());

            if (apiResponse.isServerError()) {
                throw new CurrencyApiServerErrorException(apiResponse.statusCode(), apiResponse.body());
            }
            if (apiResponse.isClientError()) {
                throw new CurrencyApiClientErrorException(apiResponse.statusCode(), apiResponse.body());
            }

            return apiResponse;
        } catch (final UnirestException e) {
            throw new CurrencyApiConnectionException("Could not reach the currency exchange API at " + url, e);
        }
    }
}
