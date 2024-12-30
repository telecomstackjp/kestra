package io.kestra.core.http.client.configurations;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kestra.core.models.annotations.PluginProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.auth.BearerToken;
import org.apache.hc.client5.http.auth.CredentialsProvider;

@Getter
@SuperBuilder(toBuilder = true)
public class BearerAuthConfiguration extends AbstractAuthConfiguration {
    @NotNull
    @JsonInclude
    @Builder.Default
    protected String type = "BEARER";

    @Schema(title = "The password for HTTP basic authentication.")
    @PluginProperty(dynamic = true)
    private final String token;

    @Override
    public CredentialsProvider credentials() {
        BearerToken bearerToken = new BearerToken(this.token);

        return (authScope, context) -> bearerToken;
    }
}
