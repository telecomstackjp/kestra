package io.kestra.core.http.client.configurations;

import io.kestra.core.models.annotations.PluginProperty;
import io.micronaut.logging.LogLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Builder(toBuilder = true)
@Getter
public class HttpConfiguration {
    @Schema(title = "The timeout configuration.")
    @PluginProperty
    private TimeoutConfiguration timeout;

    @Schema(title = "The proxy configuration.")
    @PluginProperty
    private ProxyConfiguration proxy;

    @Schema(title = "The authentification to use.")
    private AbstractAuthConfiguration auth;

    @Schema(title = "The SSL request options")
    private SslOptions ssl;

    @Schema(title = "Whether redirects should be followed automatically.")
    @Builder.Default
    @PluginProperty
    private Boolean followRedirects = true;

    @Schema(title = "If true, allow a failed response code (response code >= 400)")
    @Builder.Default
    @PluginProperty
    private Boolean allowFailed = false;

    @Deprecated
    public void setConnectTimeout(Duration connectTimeout) {
        if (this.timeout == null) {
            this.timeout = TimeoutConfiguration.builder()
                .build();
        }

        this.timeout = this.timeout.toBuilder()
            .connectTimeout(connectTimeout)
            .build();
    }

    @Deprecated
    public void setReadTimeout(Duration readTimeout) {
        if (this.timeout == null) {
            this.timeout = TimeoutConfiguration.builder()
                .build();
        }

        this.timeout = this.timeout.toBuilder()
            .readIdleTimeout(readTimeout)
            .build();
    }

    @Deprecated
    public void setProxyType(Proxy.Type proxyType) {
        if (this.proxy == null) {
            this.proxy = ProxyConfiguration.builder()
                .build();
        }

        this.proxy = this.proxy.toBuilder()
            .type(proxyType)
            .build();
    }

    @Deprecated
    public void setProxyAddress(String proxyAddress) {
        if (this.proxy == null) {
            this.proxy = ProxyConfiguration.builder()
                .build();
        }

        this.proxy = this.proxy.toBuilder()
            .address(proxyAddress)
            .build();
    }

    @Deprecated
    public void setProxyPort(Integer proxyPort) {
        if (this.proxy == null) {
            this.proxy = ProxyConfiguration.builder()
                .build();
        }

        this.proxy = this.proxy.toBuilder()
            .port(proxyPort)
            .build();
    }

    @Deprecated
    public void setProxyUsername(String proxyUsername) {
        if (this.proxy == null) {
            this.proxy = ProxyConfiguration.builder()
                .build();
        }

        this.proxy = this.proxy.toBuilder()
            .username(proxyUsername)
            .build();
    }

    @Deprecated
    public void setProxyPassword(String proxyPassword) {
        if (this.proxy == null) {
            this.proxy = ProxyConfiguration.builder()
                .build();
        }

        this.proxy = this.proxy.toBuilder()
            .password(proxyPassword)
            .build();
    }

    @Deprecated
    public void setBasicAuthUser(String basicAuthUser) {
        if (this.auth == null || !(this.auth instanceof BasicAuthConfiguration)) {
            this.auth = BasicAuthConfiguration.builder()
                .build();
        }

        this.auth = ((BasicAuthConfiguration) this.auth).toBuilder()
            .username(basicAuthUser)
            .build();
    }

    @Deprecated
    private void setBasicAuthPassword(String basicAuthPassword) {
        if (this.auth == null || !(this.auth instanceof BasicAuthConfiguration)) {
            this.auth = BasicAuthConfiguration.builder()
                .build();
        }

        this.auth = ((BasicAuthConfiguration) this.auth).toBuilder()
            .password(basicAuthPassword)
            .build();
    }

    @Schema(title = "The default charset for the request.")
    @Builder.Default
    @PluginProperty
    private final Charset defaultCharset = StandardCharsets.UTF_8;

    @Schema(title = "The log level for the HTTP client.")
    @PluginProperty
    private final LogLevel logLevel;


    @Deprecated
    private void setReadIdleTimeout(Duration readIdleTimeout) {

    }

    @Deprecated
    public void setConnectionPoolIdleTimeout(Duration readTimeout) {

    }

    @Deprecated
    public void setMaxContentLength(Integer maxContentLength) {

    }
}
