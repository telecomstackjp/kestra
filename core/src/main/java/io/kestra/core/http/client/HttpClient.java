package io.kestra.core.http.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.apache.FailedResponseInterceptor;
import io.kestra.core.http.client.apache.LoggingRequestInterceptor;
import io.kestra.core.http.client.apache.LoggingResponseInterceptor;
import io.kestra.core.http.client.apache.RunContextResponseInterceptor;
import io.kestra.core.http.client.configurations.HttpConfiguration;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

@Slf4j
public class HttpClient implements Closeable {
    private transient CloseableHttpClient client;
    private final RunContext runContext;
    private final HttpConfiguration configuration;

    @Builder
    public HttpClient(RunContext runContext, @Nullable HttpConfiguration configuration) {
        this.runContext = runContext;
        this.configuration = configuration == null ? HttpConfiguration.builder().build() : configuration;
    }

    private CloseableHttpClient client() throws IllegalVariableEvaluationException {
        if (this.client != null) {
            return this.client;
        }

        org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder = HttpClients.custom()
            .disableDefaultUserAgent()
            .setUserAgent("Kestra");

        // logger
        if (runContext.logger() != null) {
            if (ArrayUtils.contains(this.configuration.getLogs(), HttpConfiguration.LoggingType.REQUEST_HEADERS) ||
                ArrayUtils.contains(this.configuration.getLogs(), HttpConfiguration.LoggingType.REQUEST_BODY)
            ) {
                builder.addRequestInterceptorLast(new LoggingRequestInterceptor(runContext.logger(), this.configuration.getLogs()));
            }

            if (ArrayUtils.contains(this.configuration.getLogs(), HttpConfiguration.LoggingType.RESPONSE_HEADERS) ||
                ArrayUtils.contains(this.configuration.getLogs(), HttpConfiguration.LoggingType.RESPONSE_BODY)
            ) {
                builder.addResponseInterceptorLast(new LoggingResponseInterceptor(runContext.logger(), this.configuration.getLogs()));
            }
        }

        // Object dependencies
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom();

        // Timeout
        if (this.configuration.getTimeout() != null) {
            if (this.configuration.getTimeout().getConnectTimeout() != null) {
                connectionConfig.setConnectTimeout(Timeout.of(this.configuration.getTimeout().getConnectTimeout()));
            }

            if (this.configuration.getTimeout().getReadIdleTimeout() != null) {
                connectionConfig.setSocketTimeout(Timeout.of(this.configuration.getTimeout().getReadIdleTimeout()));
            }
        }

        // proxy
        if (this.configuration.getProxy() != null) {
            // @TODO use CustomSocketFactory

            if (this.configuration.getProxy().getUsername() != null && this.configuration.getProxy().getPassword() != null) {
                builder.setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy());

                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    new AuthScope(
                        runContext.render(this.configuration.getProxy().getAddress()),
                        this.configuration.getProxy().getPort()
                    ),
                    new UsernamePasswordCredentials(
                        runContext.render(this.configuration.getProxy().getUsername()),
                        runContext.render(this.configuration.getProxy().getPassword()).toCharArray()
                    )
                );

                builder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        // ssl
        if (this.configuration.getSsl() != null) {
            if (this.configuration.getSsl().getInsecureTrustAllCertificates() != null) {
                connectionManagerBuilder.setSSLSocketFactory(this.selfSignedConnectionSocketFactory());
            }
        }

        // root options
        if (!this.configuration.getFollowRedirects()) {
            builder.disableRedirectHandling();
        }

        if (!this.configuration.getAllowFailed()) {
            builder.addResponseInterceptorLast(new FailedResponseInterceptor());
        }

        builder.addResponseInterceptorLast(new RunContextResponseInterceptor(this.runContext));

        // builder object
        connectionManagerBuilder.setDefaultConnectionConfig(connectionConfig.build());
        builder.setConnectionManager(connectionManagerBuilder.build());

        this.client = builder.build();

        return client;
    }

    private SSLConnectionSocketFactory selfSignedConnectionSocketFactory() {
        try {
            SSLContext sslContext = SSLContexts
                .custom()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

            return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Send a request
     *
     * @param request the request
     * @param cls the class of the response
     * @param <T> the type of response expected
     * @return the response
     */
    public <T> HttpResponse<T> request(HttpRequest request, Class<T> cls) throws HttpClientException, IllegalVariableEvaluationException {
        HttpClientContext httpClientContext = HttpClientContext.create();

        return this.request(request, httpClientContext, r -> {
            T body = bodyHandler(cls, r.getEntity());

            return HttpResponse.from(r, body, request, httpClientContext);
        });
    }

    /**
     * Send a request and expect a json response
     *
     * @param request the request
     * @param <T> the type of response expected
     * @return the response
     */
    public <T> HttpResponse<T> request(HttpRequest request) throws HttpClientException, IllegalVariableEvaluationException {
        HttpClientContext httpClientContext = HttpClientContext.create();

        return this.request(request, httpClientContext, response -> {
            T body = JacksonMapper.ofJson().readValue(response.getEntity().getContent(), new TypeReference<>() {});

            return HttpResponse.from(response, body, request, httpClientContext);
        });
    }

    @SuppressWarnings("resource")
    private <T> HttpResponse<T> request(
        HttpRequest request,
        HttpClientContext httpClientContext,
        HttpClientResponseHandler<HttpResponse<T>> responseHandler
    ) throws HttpClientException, IllegalVariableEvaluationException {
        try {
            return client().execute(request.to(), httpClientContext, responseHandler);
        } catch (SocketException e) {
            throw new HttpClientRequestException(e.getMessage(), request, e);
        } catch (IOException e) {
            if (e.getCause() instanceof HttpClientException httpClientException) {
                throw httpClientException;
            }

            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T bodyHandler(Class<?> cls, HttpEntity entity) throws IOException, ParseException {
        if (entity == null) {
            return null;
        } else if (cls.isAssignableFrom(String.class)) {
            return (T) EntityUtils.toString(entity);
        } else if (cls.isAssignableFrom(Byte[].class)) {
            return (T) ArrayUtils.toObject(EntityUtils.toByteArray(entity));
        } else if (cls.isAssignableFrom(InputStream.class)) {
            return (T) entity.getContent();
        } else {
            return (T) JacksonMapper.ofJson().readValue(entity.getContent(), cls);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.client != null) {
            this.client.close();
        }
    }
}
