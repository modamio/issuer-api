package es.puig.issuer.infrastructure.controller;

import es.puig.issuer.application.workflow.impl.DeferredCredentialWorkflow;
import es.puig.issuer.domain.model.dto.PendingCredentials;
import es.puig.issuer.domain.model.dto.SignedCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/deferred-credentials")
@RequiredArgsConstructor
public class DeferredCredentialController {

    private final DeferredCredentialWorkflow deferredCredentialWorkflow;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<PendingCredentials> getUnsignedCredentials(@RequestHeader(value = "X-SSL-Client-Cert") String clientCert) {
        // todo: implement clientCert validation through Keycloak before executing the following code
        log.debug(clientCert);
        return deferredCredentialWorkflow.getPendingCredentialsByOrganizationId(clientCert);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateCredentials(@RequestHeader(value = "X-SSL-Client-Cert") String clientCert,
                                        @RequestBody SignedCredentials signedCredentials) {
        // todo: implement clientCert validation through Keycloak before executing the following code
        log.debug(clientCert);
        return deferredCredentialWorkflow.updateSignedCredentials(signedCredentials);
    }

}
