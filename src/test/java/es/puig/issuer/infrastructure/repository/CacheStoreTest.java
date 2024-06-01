package es.puig.issuer.infrastructure.repository;

import static org.mockito.Mockito.*;
import com.google.common.cache.Cache;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

class CacheStoreTest {

    private CacheStore<String> cacheStore;
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        // Mock the cache since we are only testing the CacheStore logic, not the cache itself
        cache = mock(Cache.class);
        // Build the cache store with arbitrary expiry and TimeUnit
        cacheStore = new CacheStore<>(1, TimeUnit.MINUTES);
        // Inject the mocked cache into our CacheStore
        ReflectionTestUtils.setField(cacheStore, "cache", cache);
    }

    @Test
    void testAddValidData() {
        String key = "key1";
        String value = "value1";
        when(cache.getIfPresent(key)).thenReturn(value);

        StepVerifier.create(cacheStore.add(key, value))
                .expectSubscription()
                .expectNext(key)
                .verifyComplete();

        verify(cache).put(key, value);
    }

    @Test
    void testGetExistingKey() {
        String key = "key1";
        String expectedValue = "value1";
        when(cache.getIfPresent(key)).thenReturn(expectedValue);

        StepVerifier.create(cacheStore.get(key))
                .expectSubscription()
                .expectNext(expectedValue)
                .verifyComplete();

        verify(cache).getIfPresent(key);
    }

    @Test
    void testDeleteKey() {
        String key = "key1";
        doNothing().when(cache).invalidate(key);

        cacheStore.delete(key);

        verify(cache).invalidate(key);
    }

    @Test
    void testAddNullKeyOrValue() {
        // Test with null key
        StepVerifier.create(cacheStore.add(null, "value"))
                .expectSubscription()
                .verifyComplete(); // Verify that it completes without emitting any value

        // Test with null value
        StepVerifier.create(cacheStore.add("key", null))
                .expectSubscription()
                .verifyComplete(); // Verify that it completes without emitting any value

        // Ensure that no put operation was performed in the cache
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void testGetNonExistingKey() {
        String key = "nonExisting";
        when(cache.getIfPresent(key)).thenReturn(null);

        StepVerifier.create(cacheStore.get(key))
                .expectSubscription()
                .verifyComplete(); // Verify that it completes without emitting any value

        verify(cache).getIfPresent(key);
    }
}
