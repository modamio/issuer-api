package es.puig.issuer.domain.service;

import es.puig.issuer.domain.model.dto.VcTemplate;
import es.puig.issuer.domain.service.impl.CredentialSchemaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CredentialSchemaServiceImplTest {

    @InjectMocks
    private CredentialSchemaServiceImpl issuerVcTemplateService;
    private final List<String> vcTemplateNames = Arrays.asList(
            "LEARCredentialEmployee"
    );

    @Test
    void getAllVcTemplates() {
        Mono<List<VcTemplate>> resultMono = issuerVcTemplateService.getAllVcTemplates();
        List<VcTemplate> result = resultMono.block();

        assertEquals(vcTemplateNames.size(), Objects.requireNonNull(result).size());
    }

    @Test
    void getAllDetailedVcTemplates() {
        Mono<List<VcTemplate>> resultMono = issuerVcTemplateService.getAllDetailedVcTemplates();
        List<VcTemplate> result = resultMono.block();

        assertEquals(vcTemplateNames.size(), Objects.requireNonNull(result).size());
    }

    @Test
    void getTemplate() {
        Mono<VcTemplate> resultMono = issuerVcTemplateService.getTemplate("LegalPerson");
        VcTemplate result = resultMono.block();

        assertEquals("LegalPerson", Objects.requireNonNull(result).name());
    }

}
