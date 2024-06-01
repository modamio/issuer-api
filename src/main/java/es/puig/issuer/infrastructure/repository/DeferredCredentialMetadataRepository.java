package es.puig.issuer.infrastructure.repository;

import es.puig.issuer.domain.model.entities.DeferredCredentialMetadata;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DeferredCredentialMetadataRepository extends ReactiveCrudRepository<DeferredCredentialMetadata, UUID> {

    Mono<DeferredCredentialMetadata> findByTransactionId(String transactionId);
    Mono<DeferredCredentialMetadata> findByTransactionCode(String transactionCode);
    Mono<DeferredCredentialMetadata> findByAuthServerNonce(String authServerNonce);
    Mono<DeferredCredentialMetadata> findByProcedureId(UUID procedureId);
}
