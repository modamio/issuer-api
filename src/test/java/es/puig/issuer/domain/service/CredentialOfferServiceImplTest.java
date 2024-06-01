package es.puig.issuer.domain.service;

import es.puig.issuer.domain.model.dto.Grant;
import es.puig.issuer.domain.service.impl.CredentialOfferServiceImpl;
import es.puig.issuer.infrastructure.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static es.puig.issuer.domain.util.Constants.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferServiceImplTest {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private CredentialOfferServiceImpl credentialOfferService;

    @BeforeEach
    void setUp() {
        when(appConfig.getIssuerApiExternalDomain()).thenReturn("https://example.com");
    }

    @Test
    void testBuildCustomCredentialOffer() {
        String credentialType = "type1";
        String preAuthCode = "code123";
        Grant grant = Grant.builder().preAuthorizedCode(preAuthCode).txCode(Grant.TxCode.builder().length(4).build()).build();
        when(appConfig.getIssuerApiExternalDomain()).thenReturn("https://example.com");
        StepVerifier.create(credentialOfferService.buildCustomCredentialOffer(credentialType, grant))
                .expectNextMatches(offer ->
                        offer.credentialIssuer().equals("https://example.com") &&
                                offer.credentialConfigurationIds().equals(List.of(STUDENT_CREDENTIAL)) &&
                                offer.grants().containsKey(GRANT_TYPE) &&
                                offer.grants().get(GRANT_TYPE).preAuthorizedCode().equals(preAuthCode) &&
                                offer.grants().get(GRANT_TYPE).txCode().length() == 4
                )
                .verifyComplete();
    }

    @Test
    void testCreateCredentialOfferUri() {
        String nonce = "abc123";
        Mono<String> result = credentialOfferService.createCredentialOfferUri(nonce);
        StepVerifier.create(result)
                .expectNext("openid-credential-offer://?credential_offer_uri=https%3A%2F%2Fexample.com%2Fapi%2Fv1%2Fcredential-offer%2Fabc123")
                .verifyComplete();
    }

}