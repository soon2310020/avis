package com.stg.service.caching;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.stg.repository.QuotationHeaderRepository;
import com.stg.service.caching.base.CachingService;
import com.stg.service.dto.quotation.QuotationDto;

import io.lettuce.core.api.sync.RedisCommands;

@Service
public class FlexibleQuotationCaching extends CachingService {
    private final QuotationHeaderRepository quotationHeaderRepository;

    public FlexibleQuotationCaching(RedisCommands<String, String> redis, QuotationHeaderRepository quotationHeaderRepository) {
        super(redis);
        this.quotationHeaderRepository = quotationHeaderRepository;
    }

    /***/
    public boolean existsProcess(String username, long processId) {
        return redis.exists(KEY.quotationDataKey(username, processId)) > 0 ||
                quotationHeaderRepository.findFirstByCreatedByAndProcessIdOrderByIdDesc(username, processId).isPresent();
    }


    /**
     * QuotationData
     */
    public boolean setQuotationData(String username, long processId, QuotationDto value) {
        return this.set(KEY.quotationDataKey(username, processId), value, TTL_DEFAULT);
    }

    public QuotationDto getQuotationData(String username, long processId) {
        QuotationDto data = this.get(KEY.quotationDataKey(username, processId), QuotationDto.class);
        if (data == null) {
            String rawData = quotationHeaderRepository.findFirstRawDataByCreatedByAndProcessId(username, processId);
            if (StringUtils.hasText(rawData)) {
                data = JACKSON.fromJson(rawData, QuotationDto.class);
            }
        }
        return data;
    }

    public boolean delQuotationData(String username, long processId) {
        return redis.del(KEY.quotationDataKey(username, processId)) > 0;
    }

    /**
     * Direct Id
     */
    public boolean setDirectId(String username, long processId, Long directId) {
        return this.set(KEY.directKey(username, processId), directId, TTL_DEFAULT);
    }
    
    public Long getDirectId(String username, long processId) {
        return this.get(KEY.directKey(username, processId), Long.class);
    }
    
    public boolean delDirectId(String username, long processId) {
        return redis.del(KEY.directKey(username, processId)) > 0;
    }
    
    /**
     * ==================== GEN-KEY ====================
     */
    private static final String FLEX_PROCESS_PREFIX = "FLEX_PRC:";

    @SuppressWarnings("unused")
    private static final class KEY {
        public static String processKey(String username) {
            return FLEX_PROCESS_PREFIX + username;
        }

        public static String customerAssuredKey(String username, long processId) {
            return processKey(username) + ":C_ASSURED:" + processId;
        }

        public static String rmKey(String username, long processId) {
            return processKey(username) + ":SP_CRM:" + processId;
        }

        public static String icKey(String username, long processId) {
            return processKey(username) + ":SP_IC:" + processId;
        }

        public static String supporterKey(String username, long processId) {
            return processKey(username) + ":SP_SUP:" + processId;
        }

        public static String quotationDataKey(String username, long processId) {
            return processKey(username) + ":QUOTE:" + processId;
        }
        
        public static String directKey(String username, long processId) {
            return processKey(username) + ":DIRECT:" + processId;
        }

    }
}
