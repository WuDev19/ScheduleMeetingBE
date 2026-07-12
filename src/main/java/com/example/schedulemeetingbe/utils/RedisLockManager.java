package com.example.schedulemeetingbe.utils;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:room_date:";

    public RLock getLock(long key) {
        return redissonClient.getLock(LOCK_PREFIX + key);
    }

    public RLock getLocks(long[] keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Keys cannot be null or empty");
        }
        long[] sortedKeys = Arrays.copyOf(keys, keys.length);
        Arrays.sort(sortedKeys);
        List<RLock> locks = new ArrayList<>();
        for (long k : sortedKeys) {
            locks.add(redissonClient.getLock(LOCK_PREFIX + k));
        }
        if (locks.size() == 1) {
            return locks.get(0);
        } else {
            return redissonClient.getMultiLock(locks.toArray(new RLock[0]));
        }
    }

}
