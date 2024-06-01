package es.puig.issuer.infrastructure.repository;

import es.puig.issuer.domain.model.entities.CredentialProcedure;
import es.puig.issuer.domain.model.enums.CredentialStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialProcedureRepository extends ReactiveCrudRepository<CredentialProcedure, UUID> {
    Flux<CredentialProcedure> findByCredentialStatusAndOrganizationIdentifier(CredentialStatus credentialStatus, String organizationIdentifier);
    Flux<CredentialProcedure> findAllByOrganizationIdentifier(String organizationIdentifier);
    Mono<CredentialProcedure> findByProcedureIdAndOrganizationIdentifier(UUID procedureId, String organizationIdentifier);
    Mono<CredentialProcedure> findByCredentialId(UUID credentialId);
}
