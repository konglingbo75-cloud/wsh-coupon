package com.wsh.common.redis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wsh.redis.mock", havingValue = "false", matchIfMissing = true)
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== String ==========

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ========== Atomic ==========

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * Redis 原子库存扣减（Lua 脚本，防超卖）
     *
     * @return 剩余库存，-1 表示库存不足
     */
    public Long stockDecrement(String key, int count) {
        String script =
                "local stock = tonumber(redis.call('get', KEYS[1])) " +
                "if stock == nil then return -1 end " +
                "if stock >= tonumber(ARGV[1]) then " +
                "  return redis.call('decrby', KEYS[1], ARGV[1]) " +
                "else " +
                "  return -1 " +
                "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        return redisTemplate.execute(redisScript,
                Collections.singletonList(key), count);
    }

    // ========== Set ==========

    public Long setAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean setIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // ========== 分布式锁（简易版，复杂场景用 Redisson） ==========

    /**
     * 尝试获取锁
     */
    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 释放锁（Lua 脚本保证原子性）
     */
    public Boolean releaseLock(String key, String value) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "  return redis.call('del', KEYS[1]) " +
                "else " +
                "  return 0 " +
                "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript,
                Collections.singletonList(key), value);
        return result != null && result > 0;
    }
}
