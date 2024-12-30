package io.kestra.core.http.client.configurations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.auth.CredentialsProvider;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BasicAuthConfiguration.class, name = "BASICAUTH"),
    @JsonSubTypes.Type(value = BearerAuthConfiguration.class, name = "BEARER")
})
@SuperBuilder(toBuilder = true)
public abstract class AbstractAuthConfiguration {
    abstract public String getType();

    abstract public CredentialsProvider credentials();
}
