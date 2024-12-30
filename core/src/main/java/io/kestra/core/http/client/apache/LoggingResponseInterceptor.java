package io.kestra.core.http.client.apache;

import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;

import java.io.IOException;

@AllArgsConstructor
public class LoggingResponseInterceptor extends AbstractLoggingInterceptor implements HttpResponseInterceptor {
    Logger logger;

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(buildResponseEntry(response));
            logger.debug(buildHeadersEntry("response", response.getHeaders()));
        }

        if (logger.isTraceEnabled()) {
            logger.trace(buildEntityEntry("response", response instanceof ClassicHttpResponse classicHttpResponse ? classicHttpResponse : null));
        }
    }

    private static String buildResponseEntry(HttpResponse response) {
        return "response:" +
            "\n    reason: "+ response.getReasonPhrase();
    }
}
