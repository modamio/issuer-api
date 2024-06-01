package es.puig.issuer.domain.service.impl;

import es.puig.issuer.domain.exception.TransactionCodeNotFoundException;
import es.puig.issuer.domain.model.dto.DeferredCredentialMetadataDeferredResponse;
import es.puig.issuer.domain.model.entities.DeferredCredentialMetadata;
import es.puig.issuer.domain.service.DeferredCredentialMetadataService;
import es.puig.issuer.infrastructure.repository.CacheStore;
import es.puig.issuer.infrastructure.repository.DeferredCredentialMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static es.puig.issuer.domain.util.Utils.generateCustomNonce;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeferredCredentialMetadataServiceImpl implements DeferredCredentialMetadataService {
    private final DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;
    private final CacheStore<String> cacheStore;

    @Override
    public Mono<Void> validateTransactionCode(String transactionCode){
        return cacheStore.get(transactionCode).flatMap(transaction -> {
            if(transaction != null){
                log.debug("The transaction code {} was consumed", transaction);
                cacheStore.delete(transaction);
                return Mono.empty();
            }
            else {
                return Mono.error(new TransactionCodeNotFoundException("Session not found for transaction code: " + transactionCode));
            }
        }
        );
    }

    @Override
    public Mono<Void> updateAuthServerNonceByAuthServerNonce(String accessToken, String preAuthCode) {
        return deferredCredentialMetadataRepository.findByAuthServerNonce(preAuthCode)
                .flatMap(deferredCredentialMetadata -> {
                    log.debug("Entity with: " + preAuthCode + "found");
                    deferredCredentialMetadata.setAuthServerNonce(accessToken);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then();
                })
                .doOnError(error -> log.debug("Entity with: " + preAuthCode + " not found"));
    }

    @Override
    public Mono<String> createDeferredCredentialMetadata(String procedureId) {
        return generateCustomNonce()
                .flatMap(nonce -> cacheStore.add(nonce, nonce))
                .flatMap(transactionCode -> {
                    DeferredCredentialMetadata deferredCredentialMetadata = DeferredCredentialMetadata
                            .builder()
                            .procedureId(UUID.fromString(procedureId))
                            .transactionCode(transactionCode)
                            .build();
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then(Mono.just(transactionCode));
                });
    }

    @Override
    public Mono<String> getProcedureIdByTransactionCode(String transactionCode) {
        return deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)
                .flatMap(deferredCredentialMetadata -> Mono.just(deferredCredentialMetadata.getProcedureId().toString()));
    }

    @Override
    public Mono<String> getProcedureIdByAuthServerNonce(String authServerNonce) {
        return deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)
                .flatMap(deferredCredentialMetadata -> Mono.just(deferredCredentialMetadata.getProcedureId().toString()));
    }

    @Override
    public Mono<Void> updateAuthServerNonceByTransactionCode(String transactionCode, String authServerNonce) {
        return deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setAuthServerNonce(authServerNonce);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then();
                });
    }

    @Override
    public Mono<String> updateDeferredCredentialMetadataByAuthServerNonce(String authServerNonce, String format) {
        String transactionId = UUID.randomUUID().toString();
        return deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setTransactionId(transactionId);
                    deferredCredentialMetadata.setVcFormat(format);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then(Mono.just(transactionId));
                })
                .doOnSuccess(result -> log.info("Updated deferred"));
    }

    @Override
    public Mono<Void> updateVcByProcedureId(String vc, String procedureId) {
        return deferredCredentialMetadataRepository.findByProcedureId(UUID.fromString(procedureId))
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setVc(vc);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then();
                });
    }

    @Override
    public Mono<DeferredCredentialMetadataDeferredResponse> getVcByTransactionId(String transactionId) {
        return deferredCredentialMetadataRepository.findByTransactionId(transactionId)
                .flatMap(deferredCredentialMetadata -> {
                    if (deferredCredentialMetadata.getVc() != null){
                        return Mono.just(DeferredCredentialMetadataDeferredResponse.builder()
                                .vc(deferredCredentialMetadata.getVc())
                                .id(deferredCredentialMetadata.getId().toString())
                                .build());
                    }
                    else {
                        String freshTransactionId = UUID.randomUUID().toString();
                        deferredCredentialMetadata.setTransactionId(freshTransactionId);
                        return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                                .flatMap(deferredCredentialMetadata1 -> Mono.just(DeferredCredentialMetadataDeferredResponse.builder().transactionId(
                                        deferredCredentialMetadata1.getTransactionId())
                                        .id(deferredCredentialMetadata.getId().toString())
                                        .build()));
                    }
                });
    }

    @Override
    public Mono<Void> deleteDeferredCredentialMetadataById(String id) {
        return deferredCredentialMetadataRepository.deleteById(UUID.fromString(id));
    }

}
