package io.kestra.core.http.client;

import lombok.Getter;
import org.apache.hc.core5.http.HttpException;

@Getter
public abstract class HttpClientException extends HttpException {
    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
