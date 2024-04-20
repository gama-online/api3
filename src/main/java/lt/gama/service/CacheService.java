package lt.gama.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Service
public class CacheService<K, V> {

    private static volatile Cache<String, Object> cache;

    private static Cache<String, Object> cache() {
        var localRef = cache;
        if (localRef == null) {
            synchronized (CacheService.class) {
                localRef = cache;
                if (localRef == null) {
                    cache = localRef = CacheBuilder.newBuilder()
                            .expireAfterAccess(Duration.of(1, ChronoUnit.HOURS))
                            .build();
                }
            }
        }
        return localRef;
    }


    public ICache<K, V> cache(String prefix) {
        return new ICache<>() {

            private final String _prefix = prefix + '\u0001';
            @SuppressWarnings("unchecked")
            private final Cache<String, V> cache = (Cache<String, V>) cache();

            private String keyOf(K key) {
                return _prefix + key;
            }

            @Override
            public V get(K key) {
                return cache.getIfPresent(keyOf(key));
            }

            @Override
            public V put(K key, V value) {
                cache.put(keyOf(key), value);
                return value;
            }

            @Override
            public void remove(K key) {
                cache.invalidate(keyOf(key));
            }
        };
    }

    interface ICache<K, V> {
        V get(K key);
        V put(K key, V value);
        void remove(K key);
    }
}
