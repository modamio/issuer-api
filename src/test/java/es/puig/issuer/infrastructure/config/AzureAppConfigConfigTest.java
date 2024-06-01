package es.puig.issuer.infrastructure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import es.puig.issuer.infrastructure.config.AzAppConfigurationConfig;
import es.puig.issuer.infrastructure.config.properties.AzureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AzureAppConfigConfigTest {
    @Mock
    private AzureProperties azureProperties;

    @InjectMocks
    private AzAppConfigurationConfig azAppConfigurationConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAzureTokenCredential() {
        // Execution
        TokenCredential tokenCredential = azAppConfigurationConfig.azureTokenCredential();

        // Verification
        assertNotNull(tokenCredential, "TokenCredential should not be null");
        verifyNoInteractions(azureProperties); // Ensures azureProperties is not used in this bean creation
    }

    @Test
    void testAzureConfigurationClient() {
        // Setup
        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(azureProperties.endpoint()).thenReturn("https://your-endpoint-here");

        // Execution
        ConfigurationClient configurationClient = azAppConfigurationConfig.azureConfigurationClient(tokenCredential);

        // Verification
        assertNotNull(configurationClient, "ConfigurationClient should not be null");
        verify(azureProperties, times(2)).endpoint(); // Adjusted to expect 2 invocations
    }
}
