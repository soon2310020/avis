package com.stg.service.lock.base;

public interface RedisLock {
    /***/
    <T> T using(String key, long ttlMillisecond, ExecHandler<T> handler, String reason) throws RedLockException;
    default <T> T using(String key, long ttlMillisecond, ExecHandler<T> handler) throws RedLockException {
        return using(key, ttlMillisecond, handler, null);
    }

    void using(String key, long ttlMillisecond, Runnable runnable, String reason) throws RedLockException;
    default void using(String key, long ttlMillisecond, Runnable runnable) throws RedLockException {
        using(key, ttlMillisecond, runnable, null);
    }

    /**NOTE: lock không thành công thì throws RedLockException*/
    void lock(String key, long ttlMillisecond, String reason) throws RedLockException;
    default void lock(String key, long ttlMillisecond) throws RedLockException {
        lock(key, ttlMillisecond, null);
    }

    /**
     * NOTE: both check and lock (khi chưa lock thì sẽ lock)
     */
    boolean acquire(String key, long ttlMillisecond);

    /***/
    boolean release(String key);

    /**
     * Check trạng thái đã lock chưa
     */
    boolean isLocked(String key);
}
