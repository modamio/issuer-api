package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DeferredCredentialRequest(
        @Schema(example = "958e84cf-888b-488a-bf30-7f3b14f70699", description = "Transaction id for deferred emission") @JsonProperty("transaction_id") String transactionId
) {
}
