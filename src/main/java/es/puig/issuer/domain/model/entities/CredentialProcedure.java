package es.puig.issuer.domain.model.entities;

import es.puig.issuer.domain.model.enums.CredentialStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("credentials.credential_procedure")
public class CredentialProcedure {
    @Id
    @Column("procedure_id")
    private UUID procedureId;

    @Column("credential_id")
    private UUID credentialId;

    @Column("credential_format")
    private String credentialFormat;

    @Column("credential_decoded")
    private String credentialDecoded;

    @Column("credential_encoded")
    private String credentialEncoded;

    @Column("credential_status")
    private CredentialStatus credentialStatus;

    @Column("organization_identifier")
    private String organizationIdentifier;

    @Column("updated_at")
    private Timestamp updatedAt;
}
