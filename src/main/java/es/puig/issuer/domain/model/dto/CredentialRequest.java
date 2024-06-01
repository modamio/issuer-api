package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CredentialRequest(
        @Schema(description = "Request body for creating a Verifiable Credential") @JsonProperty("format") String format,
        @JsonProperty("credential_definition") CredentialDefinition credentialDefinition,
        @JsonProperty("proof") Proof proof) {
}
