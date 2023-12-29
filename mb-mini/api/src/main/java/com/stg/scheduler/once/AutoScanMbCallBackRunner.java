package com.stg.scheduler.once;

import com.stg.service.ExternalAPIService;
import com.stg.service.lock.base.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
//@Service //INACTIVE!
@RequiredArgsConstructor
public class AutoScanMbCallBackRunner implements CommandLineRunner {
    private final ExternalAPIService externalAPIService;
    private final RedisLock redisLock;

    @Override
    public void run(String... args) {
        try {
            log.info("[AutoScanMbCallBackRunner] Starting... ");

            // lock 5m = 300000ms
            redisLock.using("AutoScanMbCallBackRunner", 300000, externalAPIService::scheduleCheckPendingTransaction);
        } catch (Exception ex) {
            log.error("[AutoScanMbCallBackRunner] Error", ex);
        }
        log.info("[AutoScanMbCallBackRunner] Ended ");
    }
}
