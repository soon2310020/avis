package com.stg.service.caching;

import com.stg.service.caching.base.gson.CachingService2;
import com.stg.service3rd.toolcrm.dto.resp.quote.DirectInfo;
import com.stg.service.dto.external.responseFlexible.FlexibleQuotationResp;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.stg.utils.Common.processIdKey;
import static com.stg.utils.FlexibleCommon.processCifCreateQuoteIdKey;

@Slf4j
@Service
public class FlexibleQuotationCaching extends CachingService2 {
    @Value("${spring.redis.cache.flex-quote-service.default-expire-time}")
    private long FLEX_QUOTE_TTL ;// 3h

    public FlexibleQuotationCaching(RedisCommands<String, String> redis) {
        super(redis);
    }

    /**
     * ProcessId
     */
    public boolean setProcessId(String username, Long processId) {
        return this.set(KEY.processKey(username), processId, FLEX_QUOTE_TTL - 30); // verify sớm => k miss các data kéo theo khi đang xử lý
    }

    public Long getProcessId(String username) {
        return this.get(KEY.processKey(username), Long.class);
    }

    public boolean delProcessId(String username) {
        return redis.del(KEY.processKey(username)) > 0;
    }


    /***/
    public boolean existsProcess(String username, long processId) {
        return redis.exists(KEY.quotationDataKey(username, processId)) > 0;
    }

    /**
     * QuotationData
     */
    public boolean setQuotationData(String username, long processId, FlexibleQuotationResp value) {
        return this.set(KEY.quotationDataKey(username, processId), value, FLEX_QUOTE_TTL);
    }

    public FlexibleQuotationResp getQuotationData(String username, long processId) {
        return this.get(KEY.quotationDataKey(username, processId), FlexibleQuotationResp.class);
    }

    public boolean delQuotationData(String username, long processId) {
        return redis.del(KEY.quotationDataKey(username, processId)) > 0;
    }


    /**
     * QuotationData
     */
    public boolean setQuotationUuid(String username, long processId, UUID value) {
        return this.set(KEY.quoteUuid(username, processId), value, FLEX_QUOTE_TTL);
    }

    public UUID getQuotationUuid(String username, long processId) {
        return this.get(KEY.quoteUuid(username, processId), UUID.class);
    }

    public boolean delQuotationUuid(String username, long processId) {
        return redis.del(KEY.quoteUuid(username, processId)) > 0;
    }

    public boolean isFromCrmTool(String username, long processId) {
        return getQuotationUuid(username, processId) != null;
    }

    public boolean setDirectInfo(String username, UUID quotationUid, DirectInfo value) {
        return this.set(KEY.quoteDirect(username, quotationUid), value, FLEX_QUOTE_TTL);
    }

    public DirectInfo getDirectInfo(String username, UUID quotationUid) {
        return this.get(KEY.quoteDirect(username, quotationUid), DirectInfo.class);
    }

    public boolean delDirectInfo(String username, UUID quotationUid) {
        return redis.del(KEY.quoteDirect(username, quotationUid)) > 0;
    }

    /**
     * ==================== GEN-KEY ====================
     */
    private static final String FLEX_PROCESS_PREFIX = "FLEX_PRC:";
    private static final class KEY {
        public static String processKey(String username) {
            return processIdKey(username);
        }

        public static String quotationDataKey(String username, long processId) {
            return processCifCreateQuoteIdKey(username, String.valueOf(processId));
        }

        public static String quoteUuid(String username, long processId) {
            return FLEX_PROCESS_PREFIX + username + ":UUID:" + processId;
        }

        public static String quoteDirect(String username, UUID quotationUid) {
            return FLEX_PROCESS_PREFIX + username + ":DIRECT:" + quotationUid;
        }
    }
}
