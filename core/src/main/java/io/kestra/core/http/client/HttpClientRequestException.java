package io.kestra.core.http.client;

import io.kestra.core.http.HttpRequest;
import lombok.Getter;

@Getter
public class HttpClientRequestException extends HttpClientException {
    protected HttpRequest request;

    public HttpClientRequestException(String message, HttpRequest request) {
        super(message);
        this.request = request;
    }

    public HttpClientRequestException(String message, HttpRequest request, Throwable cause) {
        super(message, cause);
        this.request = request;
    }
}
