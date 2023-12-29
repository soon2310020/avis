package com.stg.service.lock.impl;

import com.stg.service.lock.ImportLockService;
import com.stg.service.lock.base.RedisLock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportLockServiceImpl implements ImportLockService {
    private final RedisLock redisLock;

    /**
     * EXPIRED_TIME
     */
    private static final long IMPORT_MB_EMPLOYEE_PX = 300000; // 5m

    /***/
    @Override
    public void lockImportDataMbEmployee(String userID, Runnable runnable) {
        redisLock.using(LOCK_KEY.IMPORT_MB_EMPLOYEE + userID, IMPORT_MB_EMPLOYEE_PX, runnable);
    }

    /**
     * KEYS
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class LOCK_KEY {
        public static final String IMPORT_MB_EMPLOYEE = "IMPORT_MB_EMPLOYEE:";
    }

}
