package com.example.BE_E_commerce.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.data.redis.core. RedisTemplate;
import org. springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== STRING OPERATIONS ==========

    /**
     * Set value with expiration
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate. opsForValue().set(key, value, timeout, unit);
            log.debug("Redis SET:  {} = {}", key, value);
        } catch (Exception e) {
            log.error("Redis SET error: {}", e.getMessage());
        }
    }

    /**
     * Set value without expiration
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis SET: {} = {}", key, value);
        } catch (Exception e) {
            log.error("Redis SET error: {}", e.getMessage());
        }
    }

    /**
     * Get value
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Redis GET:  {} = {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis GET error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get value and cast to specific type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Delete key
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Redis DELETE: {}", key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis DELETE error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Delete multiple keys
     */
    public long delete(Collection<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            log.debug("Redis DELETE multiple: {}", keys);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis DELETE multiple error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Check if key exists
     */
    public boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis HASKEY error: {}", e. getMessage());
            return false;
        }
    }

    /**
     * Set expiration
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis EXPIRE error: {}", e. getMessage());
            return false;
        }
    }

    /**
     * Get TTL (Time To Live)
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Redis GETEXPIRE error:  {}", e.getMessage());
            return -1;
        }
    }

    // ========== HASH OPERATIONS ==========

    /**
     * Set hash field
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Redis HSET: {} {} = {}", key, field, value);
        } catch (Exception e) {
            log.error("Redis HSET error: {}", e.getMessage());
        }
    }

    /**
     * Get hash field
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis HGET error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get all hash fields
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis HGETALL error: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Delete hash field
     */
    public boolean hDelete(String key, Object... fields) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, fields);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("Redis HDEL error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if hash field exists
     */
    public boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Redis HEXISTS error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Increment hash field
     */
    public long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis HINCRBY error: {}", e.getMessage());
            return 0;
        }
    }

    // ========== LIST OPERATIONS ==========

    /**
     * Push to list (right)
     */
    public long lPush(String key, Object value) {
        try {
            Long result = redisTemplate.opsForList().rightPush(key, value);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis RPUSH error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Pop from list (right)
     */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Redis RPOP error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get list range
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis LRANGE error: {}", e. getMessage());
            return List.of();
        }
    }

    /**
     * Get list size
     */
    public long lSize(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size :  0;
        } catch (Exception e) {
            log.error("Redis LLEN error: {}", e.getMessage());
            return 0;
        }
    }

    // ========== SET OPERATIONS ==========

    /**
     * Add to set
     */
    public long sAdd(String key, Object... values) {
        try {
            Long result = redisTemplate. opsForSet().add(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis SADD error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get all set members
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis SMEMBERS error:  {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * Check if member in set
     */
    public boolean sIsMember(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return Boolean. TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis SISMEMBER error: {}", e. getMessage());
            return false;
        }
    }

    /**
     * Remove from set
     */
    public long sRemove(String key, Object...  values) {
        try {
            Long result = redisTemplate. opsForSet().remove(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis SREM error: {}", e. getMessage());
            return 0;
        }
    }

    // ========== SORTED SET OPERATIONS ==========

    /**
     * Add to sorted set
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            Boolean result = redisTemplate. opsForZSet().add(key, value, score);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis ZADD error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get sorted set range (by score, descending)
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            return redisTemplate. opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("Redis ZREVRANGE error: {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * Increment score in sorted set
     */
    public double zIncrementScore(String key, Object value, double delta) {
        try {
            Double result = redisTemplate.opsForZSet().incrementScore(key, value, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis ZINCRBY error: {}", e.getMessage());
            return 0;
        }
    }

    // ========== LOCK OPERATIONS ==========

    /**
     * Try to acquire lock
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(key, "LOCKED", timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis LOCK error: {}", e. getMessage());
            return false;
        }
    }

    /**
     * Release lock
     */
    public boolean releaseLock(String key) {
        return delete(key);
    }

    // ========== UTILITY ==========

    /**
     * Get all keys matching pattern
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Redis KEYS error: {}", e. getMessage());
            return Set.of();
        }
    }

    /**
     * Delete all keys matching pattern
     */
    public long deleteByPattern(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return delete(keys);
        }
        return 0;
    }
}