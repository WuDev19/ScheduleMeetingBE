package com.example.schedulemeetingbe.service.base;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface IRedisService {
    Boolean setIfAbsent(String key, Object value);
    Boolean setIfAbsent(String key, Object value, Duration ttl);
    void set(String key, Object value);
    void set(String key, Object value, Duration ttl);
    <T> T get(String key, Class<T> clazz);
    void delete(String key);
    boolean exists(String key);
    Boolean expire(String key, Duration ttl);
    Long getExpire(String key);
    Long getExpire(String key, TimeUnit timeUnit);
}
