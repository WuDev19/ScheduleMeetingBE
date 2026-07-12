package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockManager {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:room_date:";
    private static final long ACQUIRE_TIMEOUT_MS = 10000; // 10 seconds
    private static final long LEASE_TIME_MS = 60000; // 60 seconds lease time to prevent deadlock if app crashes

    public void acquireLock(long key) {
        acquireLocks(new long[]{key});
    }

    public void acquireLocks(long[] keys) {
        if (keys == null || keys.length == 0) {
            return;
        }

        // Sort keys to prevent deadlock
        long[] sortedKeys = Arrays.copyOf(keys, keys.length);
        Arrays.sort(sortedKeys);

        List<RLock> locks = new ArrayList<>();
        for (long key : sortedKeys) {
            locks.add(redissonClient.getLock(LOCK_PREFIX + key));
        }

        RLock lock;
        if (locks.size() == 1) {
            lock = locks.get(0);
        } else {
            lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
        }

        boolean acquired = false;
        try {
            acquired = lock.tryLock(ACQUIRE_TIMEOUT_MS, LEASE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorResponse.SYSTEM_UNKNOWN_ERROR);
        }

        if (!acquired) {
            log.warn("Failed to acquire lock for keys: {}", Arrays.toString(keys));
            throw new BusinessException(ErrorResponse.LOCK_ACQUISITION_TIMEOUT);
        }

        // Register synchronization to release lock after transaction completion
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    try {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    } catch (Exception e) {
                        log.error("Failed to release Redisson lock", e);
                    }
                }
            });
        } else {
            log.warn("No active transaction found, releasing Redisson lock immediately");
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("Failed to release Redisson lock", e);
            }
        }
    }
}
