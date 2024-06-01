package es.puig.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
public record ProcedureBasicInfo(
        @JsonProperty("procedure_id") UUID procedureId,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("status") String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        @JsonProperty("updated") Timestamp updated
) {
}
