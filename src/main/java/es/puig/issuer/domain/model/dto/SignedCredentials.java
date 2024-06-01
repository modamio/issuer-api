package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
@Builder
public record SignedCredentials(
        @JsonProperty("credentials") List<SignedCredential> credentials
) {

    @Builder
    public record SignedCredential(
            @JsonProperty("credential")
            String credential
    ){}

}
