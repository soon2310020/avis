package com.stg.service.lock.base;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockImpl implements RedisLock {
    private final RedisCommands<String, String> redis;

    static final String LOCK_MESSAGE_DEFAULT = "Tiến trình đang được xử lý, vui lòng chờ trong giây lát";
    static final String TRACE_MESSAGE = "[RED-LOCKED]-- keyLock={}, luồng đang xử lý, chờ... {} millisecond";

    /**
     * @param key:            lock by key
     * @param ttlMillisecond: expired time
     * @param handler:        handle logic (and handle exception)
     */
    @Override
    public <T> T using(String key, long ttlMillisecond, ExecHandler<T> handler, String reason) throws RedLockException {
        boolean hasAcquire = false;
        try {
            //1.[CHECK-LOCKED]
            hasAcquire = this.acquire(key, ttlMillisecond);
            if (!hasAcquire) {
                log.info(TRACE_MESSAGE, key, ttlMillisecond);
                throw new RedLockException(reason != null ? reason : LOCK_MESSAGE_DEFAULT, ttlMillisecond);
            }

            //2.[Handle logic]
            return handler.exec();
        } finally {
            if (hasAcquire) this.release(key); /*UNLOCK:MANUAL*/
        }
    }

    @Override
    public void using(String key, long ttlMillisecond, Runnable runnable, String reason) throws RedLockException {
        boolean hasAcquire = false;
        try {
            //1.[CHECK-LOCKED]
            hasAcquire = this.acquire(key, ttlMillisecond);
            if (!hasAcquire) {
                log.info(TRACE_MESSAGE, key, ttlMillisecond);
                if (reason != null) {
                    throw new RedLockException(reason);
                }
                throw new RedLockException(LOCK_MESSAGE_DEFAULT, ttlMillisecond);
            }

            //2.[Handle logic]
            runnable.run();
        } finally {
            if (hasAcquire) this.release(key); /*UNLOCK:MANUAL*/
        }
    }

    /**
     * NOTE: lock không thành công thì throws RedLockException
     */
    @Override
    public void lock(String key, long ttlMillisecond, String reason) throws RedLockException {
        if (!this.acquire(key, ttlMillisecond)) {
            log.info(TRACE_MESSAGE, key, ttlMillisecond);
            if (reason != null) {
                throw new RedLockException(reason);
            }
            throw new RedLockException(LOCK_MESSAGE_DEFAULT, ttlMillisecond);
        }
    }


    /**
     * NOTE:
     * + Both check and lock (khi chưa lock thì sẽ lock)
     * + Auto unlock when expired!
     */
    @Override
    public synchronized boolean acquire(String key, long ttlMillisecond) {
        return "OK".equals(redis.set(keyLock(key), "1", SetArgs.Builder.nx().px(ttlMillisecond)));
    }

    @Override
    public boolean release(String key) {
        return redis.del(keyLock(key)) > 0;
    }

    /**
     * Check trạng thái đã lock chưa
     */
    @Override
    public boolean isLocked(String key) {
        return redis.exists(keyLock(key)) > 0;
    }

    /***/
    public static final String RED_LOCK_PREFIX = "RED_LOCK:";

    private static String keyLock(String key) {
        return RED_LOCK_PREFIX + key;
    }
}
