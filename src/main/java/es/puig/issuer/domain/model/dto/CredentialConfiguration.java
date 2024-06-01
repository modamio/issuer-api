package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
@Builder
public record CredentialConfiguration(@JsonProperty("format") String format,
                                      @JsonProperty("cryptographic_binding_methods_supported") List<String> cryptographicBindingMethodsSupported,
                                      @JsonProperty("credential_signing_alg_values_supported") List<String> credentialSigningAlgValuesSupported,
                                      @JsonProperty("credential_definition") CredentialDefinition credentialDefinition) {
}
