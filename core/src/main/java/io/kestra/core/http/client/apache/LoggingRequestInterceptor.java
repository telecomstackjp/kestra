package io.kestra.core.http.client.apache;

import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;

import java.io.IOException;

@AllArgsConstructor
public class LoggingRequestInterceptor extends AbstractLoggingInterceptor implements HttpRequestInterceptor {
    protected Logger logger;

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(buildRequestEntry(request));
            logger.debug(buildHeadersEntry("request", request.getHeaders()));
        }

        if (logger.isTraceEnabled()) {
            logger.trace(buildEntityEntry("request", request instanceof ClassicHttpRequest classicHttpRequest ? classicHttpRequest : null));
        }
    }

    private String buildRequestEntry(HttpRequest request) {
        return "request:" +
            "\n    method: " + request.getMethod() +
            "\n    uri: " + request.getRequestUri();
    }
}
