package es.puig.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.puig.issuer.domain.exception.NoCredentialFoundException;
import es.puig.issuer.domain.model.dto.CredentialDetails;
import es.puig.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.puig.issuer.domain.model.dto.CredentialProcedures;
import es.puig.issuer.domain.model.dto.ProcedureBasicInfo;
import es.puig.issuer.domain.model.entities.CredentialProcedure;
import es.puig.issuer.domain.model.enums.CredentialStatus;
import es.puig.issuer.domain.service.CredentialProcedureService;
import es.puig.issuer.infrastructure.repository.CredentialProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialProcedureServiceImpl implements CredentialProcedureService {

    private final CredentialProcedureRepository credentialProcedureRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> createCredentialProcedure(CredentialProcedureCreationRequest credentialProcedureCreationRequest) {
        CredentialProcedure credentialProcedure = CredentialProcedure.builder()
                .credentialId(UUID.fromString(credentialProcedureCreationRequest.credentialId()))
                .credentialStatus(CredentialStatus.WITHDRAWN)
                .credentialDecoded(credentialProcedureCreationRequest.credentialDecoded())
                .organizationIdentifier(credentialProcedureCreationRequest.organizationIdentifier())
                .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        return credentialProcedureRepository.save(credentialProcedure)
                .map(savedCredentialProcedure -> savedCredentialProcedure.getProcedureId().toString())
                .doOnError(e -> log.error("Error saving credential procedure", e));
    }

    @Override
    public Mono<String> getCredentialTypeByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        JsonNode typeNode = credential.get("vc").get("type");
                        if (typeNode != null && typeNode.isArray()) {
                            String credentialType = null;
                            for (JsonNode type : typeNode) {
                                if (!type.asText().equals("VerifiableCredential") && !type.asText().equals("VerifiableAttestation")) {
                                    credentialType = type.asText();
                                    break;
                                }
                            }
                            return Mono.justOrEmpty(credentialType);
                        } else {
                            return Mono.error(new RuntimeException("The credential type is missing"));
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }

                });
    }

    @Override
    public Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential, String format) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialDecoded(credential);
                    credentialProcedure.setCredentialStatus(CredentialStatus.ISSUED);
                    credentialProcedure.setCredentialFormat(format);
                    credentialProcedure.setUpdatedAt(new Timestamp(Instant.now().toEpochMilli()));
                    return credentialProcedureRepository.save(credentialProcedure)
                            .doOnSuccess(result -> log.info("Updated credential"))
                            .then();
                });
    }

    @Override
    public Mono<String> getDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> Mono.just(credentialProcedure.getCredentialDecoded()));
    }

    @Override
    public Mono<String> getMandateeEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return Mono.just(credential.get("vc").get("credentialSubject").get("mandate").get("mandatee").get("email").toString());
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }

                });
    }

    @Override
    public Mono<String> getMandatorEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return Mono.just(credential.get("vc").get("credentialSubject").get("mandate").get("mandator").get("emailAddress").toString());
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }

                });
    }

    @Override
    public Flux<String> getAllIssuedCredentialByOrganizationIdentifier(String organizationIdentifier) {
        return credentialProcedureRepository.findByCredentialStatusAndOrganizationIdentifier(CredentialStatus.ISSUED, organizationIdentifier)
                .map(CredentialProcedure::getCredentialDecoded);
    }

    @Override
    public Mono<CredentialDetails> getProcedureDetailByProcedureIdAndOrganizationId(String organizationIdentifier, String procedureId) {
        return credentialProcedureRepository.findByProcedureIdAndOrganizationIdentifier(UUID.fromString(procedureId), organizationIdentifier)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found for procedureId: " + procedureId)))
                .flatMap(credentialProcedure -> {
                    try {
                        return Mono.just(CredentialDetails.builder()
                                .procedureId(credentialProcedure.getProcedureId())
                                .credentialStatus(String.valueOf(credentialProcedure.getCredentialStatus()))
                                .credential(objectMapper.readTree(credentialProcedure.getCredentialDecoded()))
                                .build());
                    } catch (JsonProcessingException e) {
                        log.warn("Error parsing credential", e);
                        return Mono.error(new JsonParseException(null, "Error parsing credential"));
                    }
                })
                .doOnError(error -> log.error("Could not load credentials, error: {}", error.getMessage()));
    }

    @Override
    public Mono<String> updatedEncodedCredentialByCredentialId(String encodedCredential, String credentialId) {
        return credentialProcedureRepository.findByCredentialId(UUID.fromString(credentialId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialEncoded(encodedCredential);
                    credentialProcedure.setCredentialStatus(CredentialStatus.VALID);
                    return credentialProcedureRepository.save(credentialProcedure)
                            .then(Mono.just(credentialProcedure.getProcedureId().toString()));
                });
    }

    @Override
    public Mono<CredentialProcedures> getAllProceduresBasicInfoByOrganizationId(String organizationIdentifier) {
        return credentialProcedureRepository.findAllByOrganizationIdentifier(organizationIdentifier)
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        String subjectFullName = credential.get("vc").get("credentialSubject").get("mandate").get("mandatee").get("first_name").asText()
                                + " "
                                + credential.get("vc").get("credentialSubject").get("mandate").get("mandatee").get("last_name").asText();
                        return Mono.just(ProcedureBasicInfo.builder()
                                .procedureId(credentialProcedure.getProcedureId())
                                .fullName(subjectFullName)
                                .status(String.valueOf(credentialProcedure.getCredentialStatus()))
                                .updated(credentialProcedure.getUpdatedAt())
                                .build());
                    } catch (JsonProcessingException e) {
                        log.warn("Error processing json", e);
                        return Mono.error(new JsonParseException(null, "Error parsing credential"));
                    }
                })
                .map(procedureBasicInfo -> CredentialProcedures.CredentialProcedure.builder()
                        .credentialProcedure(procedureBasicInfo)
                        .build())
                .collectList()
                .map(CredentialProcedures::new);
    }

}
