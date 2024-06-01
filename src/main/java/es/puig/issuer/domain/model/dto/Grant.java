package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record Grant(@Schema(example = "1234") @JsonProperty("pre-authorized_code") String preAuthorizedCode,
                    @Schema(example = "true") @JsonProperty("tx_code") TxCode txCode
                    ) {
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TxCode(
            @JsonProperty("length")
            int length,
            @JsonProperty("input_mode")
            String inputMode,
            @JsonProperty("description")
            String description
    ){}
}
