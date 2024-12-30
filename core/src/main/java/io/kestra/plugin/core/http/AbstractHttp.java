package io.kestra.plugin.core.http;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.http.client.apache.FailedResponseInterceptor;
import io.kestra.core.http.client.apache.LoggingResponseInterceptor;
import io.kestra.core.http.client.apache.LoggingRequestInterceptor;
import io.kestra.core.http.client.apache.RunContextResponseInterceptor;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.SSLContext;

import static io.kestra.core.utils.Rethrow.throwFunction;

@Slf4j
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
abstract public class AbstractHttp extends Task implements HttpInterface {
    @NotNull
    protected String uri;

    @Builder.Default
    protected HttpMethod method = HttpMethod.GET;

    protected String body;

    protected Map<String, Object> formData;

    @Builder.Default
    protected String contentType = MediaType.APPLICATION_JSON;

    protected Map<CharSequence, CharSequence> headers;

    protected RequestOptions options;

    protected SslOptions sslOptions;

    @Builder.Default
    private Boolean allowFailed = false;

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

    protected CloseableHttpClient client(RunContext runContext) throws IllegalVariableEvaluationException, MalformedURLException, URISyntaxException {
        HttpClientBuilder builder = HttpClients.custom();
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

        if (this.options != null) {
            ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom();

            if (this.options.getConnectTimeout() != null) {
                connectionConfig.setConnectTimeout(Timeout.of(this.options.getConnectTimeout()));
            }

            if (this.options.getReadIdleTimeout() != null) {
                connectionConfig.setSocketTimeout(Timeout.of(this.options.getReadIdleTimeout()));
            }


            if (this.options.getProxyAddress() != null && this.options.getProxyPort() != null) {
                // @TODO use CustomSocketFactory

                if (this.options.getProxyUsername() != null && this.options.getProxyPassword() != null) {
                    builder.setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy());

                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                        new AuthScope(
                            runContext.render(this.options.getProxyAddress()),
                            this.options.getProxyPort()
                        ),
                        new UsernamePasswordCredentials(
                            runContext.render(this.options.getProxyUsername()),
                            runContext.render(this.options.getProxyPassword()).toCharArray()
                        )
                    );

                    builder.setDefaultCredentialsProvider(credentialsProvider);
                }
            }

            if (this.options.getFollowRedirects() != null && !this.options.getFollowRedirects()) {
                builder.disableRedirectHandling();
            }

            if (this.options.getLogLevel() != null) {
                builder.addRequestInterceptorLast(new LoggingRequestInterceptor(runContext.logger()));
                builder.addResponseInterceptorLast(new LoggingResponseInterceptor(runContext.logger()));
            }

            connectionManagerBuilder.setDefaultConnectionConfig(connectionConfig.build());
        }

        if (this.sslOptions != null) {
            if (this.sslOptions.getInsecureTrustAllCertificates() != null) {
                connectionManagerBuilder.setSSLSocketFactory(this.selfSignedConnectionSocketFactory());
            }
        }

        if (!this.allowFailed) {
            builder.addResponseInterceptorLast(new FailedResponseInterceptor());
        }

        builder.setConnectionManager(connectionManagerBuilder.build());

        builder.addResponseInterceptorFirst(new RunContextResponseInterceptor(runContext));

        return builder.build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Pair<HttpUriRequest, HttpClientContext> request(RunContext runContext) throws IllegalVariableEvaluationException, URISyntaxException, IOException {
        URI from = new URI(runContext.render(this.uri));

        HttpUriRequest request = new HttpUriRequestBase(this.method.name(), from);

        HttpClientContext localContext = HttpClientContext.create();

        if (this.options != null && this.options.getBasicAuthUser() != null && this.options.getBasicAuthPassword() != null) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(
                    runContext.render(this.options.getBasicAuthUser()),
                    runContext.render(this.options.getBasicAuthPassword()).toCharArray()
                )
            );
            localContext.setCredentialsProvider(credentialsProvider);
        }

        if (this.formData != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(this.contentType)) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                for (Map.Entry<String, Object> e : this.formData.entrySet()) {
                    String key = runContext.render(e.getKey());

                    if (e.getValue() instanceof String stringValue) {
                        String render = runContext.render(stringValue);

                        if (render.startsWith("kestra://")) {
                            File tempFile = runContext.workingDir().createTempFile().toFile();

                            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                                IOUtils.copyLarge(runContext.storage().getFile(new URI(render)), outputStream);
                            }

                            builder.addPart(key, new FileBody(tempFile));
                        } else {
                            builder.addPart(key, new StringBody(render, ContentType.APPLICATION_OCTET_STREAM));
                        }
                    } else if (e.getValue() instanceof Map mapValue && ((Map<String, String>) mapValue).containsKey("name") && ((Map<String, String>) mapValue).containsKey("content")) {
                        String name = runContext.render(((Map<String, String>) mapValue).get("name"));
                        String content = runContext.render(((Map<String, String>) mapValue).get("content"));

                        File tempFile = runContext.workingDir().createTempFile().toFile();
                        File renamedFile = new File(Files.move(tempFile.toPath(), tempFile.toPath().resolveSibling(name)).toUri());

                        try (OutputStream outputStream = new FileOutputStream(renamedFile)) {
                            IOUtils.copyLarge(runContext.storage().getFile(new URI(content)), outputStream);
                        }

                        builder.addPart(key, new FileBody(renamedFile));
                    } else {
                        builder.addPart(key, new StringBody(JacksonMapper.ofJson().writeValueAsString(e.getValue()), ContentType.APPLICATION_JSON));
                    }
                }

                request.setEntity(builder.build());
            } else {
                request.setEntity(new UrlEncodedFormEntity(runContext.render(this.formData)
                    .entrySet()
                    .stream()
                    .map(e -> new BasicNameValuePair(e.getKey(), e.getValue().toString()))
                    .toList()
                ));
            }
        } else if (this.body != null) {
            request.setEntity(new StringEntity(
                runContext.render(body),
                ContentType.create(
                    runContext.render(this.contentType),
                    this.options != null ? this.options.getDefaultCharset() : StandardCharsets.UTF_8)
                )
            );
        }

        if (this.headers != null) {
            request.setHeaders(this.headers
                .entrySet()
                .stream()
                .map(throwFunction(e -> new BasicHeader(
                    e.getKey().toString(),
                    runContext.render(e.getValue().toString())
                )))
                .toArray(Header[]::new)
            );
        }

        return Pair.of(request, localContext);
    }
}
