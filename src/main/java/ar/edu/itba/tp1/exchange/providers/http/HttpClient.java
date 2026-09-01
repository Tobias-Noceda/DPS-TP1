package ar.edu.itba.tp1.exchange.providers.http;

import java.util.Map;

public interface HttpClient {
    HttpApiResponse get(String url, Map<String, String> headers, Map<String, String> queryParams);
}
