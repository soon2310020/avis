package com.stg.service.caching;

import com.stg.service.caching.base.CachingService;
import com.stg.service.dto.quotation.crm.RmExcelInfo;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RmExcelDataCaching extends CachingService {

    @Autowired
    public RmExcelDataCaching(RedisCommands<String, String> redis) {
        super(redis);
    }

    public boolean exists() {
        return existByPattern("^" + IMPORT_PREFIX);
    }


    /***/
    public boolean exists(String mbCode) {
        return redis.exists(KEY.rmKey(mbCode)) > 0;
    }

    public boolean setData(List<RmExcelInfo> values) {
        return this.mset(values);
    }

    public RmExcelInfo getData(String mbCode) {
        return this.get(KEY.rmKey(mbCode), RmExcelInfo.class);
    }

    public boolean delData(String mbCode) {
        return redis.del(KEY.rmKey(mbCode)) > 0;
    }

    public boolean delAllData() {
        return this.delKeyPattern(IMPORT_PREFIX + "*");
    }

    /**
     * ==================== GEN-KEY ====================
     */
    private static final String IMPORT_PREFIX = "ImportRmInfo:";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class KEY {
        public static String rmKey(String mbCode) {
            return IMPORT_PREFIX + mbCode;
        }
    }
}
