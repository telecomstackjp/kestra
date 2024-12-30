package io.kestra.core.http.client.configurations;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kestra.core.models.annotations.PluginProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;

@Getter
@SuperBuilder(toBuilder = true)
public class BasicAuthConfiguration extends AbstractAuthConfiguration {
    @NotNull
    @JsonInclude
    @Builder.Default
    protected String type = "BASICAUTH";

    @Schema(title = "The username for HTTP basic authentication.")
    @PluginProperty(dynamic = true)
    private final String username;

    @Schema(title = "The password for HTTP basic authentication.")
    @PluginProperty(dynamic = true)
    private final String password;

    @Override
    public CredentialsProvider credentials() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new AuthScope(null, -1),
            new UsernamePasswordCredentials(
                this.getUsername(),
                this.getPassword().toCharArray()
            )
        );

        return credentialsProvider;
    }
}
