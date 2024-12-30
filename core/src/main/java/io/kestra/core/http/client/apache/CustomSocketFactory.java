package io.kestra.core.http.client.apache;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.core.http.HttpInterface;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class CustomSocketFactory extends SSLConnectionSocketFactory {
    private RunContext runContext;
    private HttpInterface.RequestOptions options;

    public CustomSocketFactory(final SSLContext sslContext, final HostnameVerifier hostnameVerifier) {
        super(sslContext, hostnameVerifier);
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        try {
            SocketAddress proxyAddr = new InetSocketAddress(
                runContext.render(this.options.getProxyAddress()),
                this.options.getProxyPort()
            );

            Proxy proxy = new Proxy(this.options.getProxyType(), proxyAddr);

            return new Socket(proxy);
        } catch (IllegalVariableEvaluationException e) {
            throw new IOException(e);
        }
    }

}
