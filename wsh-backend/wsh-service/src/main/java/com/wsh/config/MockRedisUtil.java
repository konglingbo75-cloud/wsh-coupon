package com.wsh.config;

import com.wsh.common.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ConcurrentHashMap-based mock of RedisUtil for local profile (no Redis server needed).
 * Overrides all methods so the parent's RedisTemplate is never touched.
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "wsh.redis.mock", havingValue = "true")
public class MockRedisUtil extends RedisUtil {

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();

    public MockRedisUtil() {
        super(new RedisTemplate<>());
        log.warn("========= MockRedisUtil active: using ConcurrentHashMap instead of Redis =========");
    }

    @Override
    public void set(String key, Object value) {
        store.put(key, value);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        store.put(key, value);
        // TTL ignored in mock
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) store.get(key);
    }

    @Override
    public Boolean delete(String key) {
        return store.remove(key) != null;
    }

    @Override
    public Long delete(List<String> keys) {
        long count = 0;
        for (String key : keys) {
            if (store.remove(key) != null) count++;
        }
        return count;
    }

    @Override
    public Boolean hasKey(String key) {
        return store.containsKey(key);
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return store.containsKey(key);
    }

    @Override
    public Long getExpire(String key) {
        return store.containsKey(key) ? -1L : -2L;
    }

    @Override
    public Long increment(String key) {
        return (Long) store.merge(key, 1L, (old, v) -> ((Number) old).longValue() + 1L);
    }

    @Override
    public Long decrement(String key) {
        return (Long) store.merge(key, -1L, (old, v) -> ((Number) old).longValue() - 1L);
    }

    @Override
    public Long stockDecrement(String key, int count) {
        synchronized (store) {
            Object val = store.get(key);
            if (val == null) return -1L;
            long stock = ((Number) val).longValue();
            if (stock >= count) {
                long remaining = stock - count;
                store.put(key, remaining);
                return remaining;
            }
            return -1L;
        }
    }

    @Override
    public Long setAdd(String key, Object... values) {
        @SuppressWarnings("unchecked")
        Set<Object> set = (Set<Object>) store.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long added = 0;
        for (Object v : values) {
            if (((Set<Object>) set).add(v)) added++;
        }
        return added;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Object> setMembers(String key) {
        Object val = store.get(key);
        return val instanceof Set ? (Set<Object>) val : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean setIsMember(String key, Object value) {
        Object val = store.get(key);
        return val instanceof Set && ((Set<Object>) val).contains(value);
    }

    @Override
    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        return store.putIfAbsent(key, value) == null;
    }

    @Override
    public Boolean releaseLock(String key, String value) {
        return store.remove(key, value);
    }

    /**
     * Set if absent (for distributed lock)
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return store.putIfAbsent(key, value) == null;
    }
}
