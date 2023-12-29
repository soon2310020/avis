package com.stg.service.caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.stg.service.caching.base.CachingService;
import com.stg.service3rd.mbal.dto.FlexibleCommon.ReferrerInput;

import io.lettuce.core.api.sync.RedisCommands;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RmInfoCaching extends CachingService {

    @Autowired
    public RmInfoCaching(RedisCommands<String, String> redis) {
        super(redis);
    }

    public boolean exists() {
        return existByPattern("^" + KEY_PREFIX);
    }


    /***/
    public boolean exists(String key) {
        return redis.exists(KEY.rmKey(key)) > 0;
    }

    public boolean setData(String key, RmCachingData info) {
        return this.set(KEY.rmKey(key), info);
    }

    public RmCachingData getData(String key) {
        return this.get(KEY.rmKey(key), RmCachingData.class);
    }

    public boolean delData(String key) {
        return redis.del(KEY.rmKey(key)) > 0;
    }

    public boolean delAllData() {
        return this.delKeyPattern(KEY_PREFIX + "*");
    }

    /**
     * ==================== GEN-KEY ====================
     */
    private static final String KEY_PREFIX = "RmInfo:";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class KEY {
        public static String rmKey(String username) {
            return KEY_PREFIX + username;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class RmCachingData {
        private String username;
        private String rmCode;
        private String rmName;
        private String rmPhoneNumber;
        private String rmEmail;
        private String rmBranchCode;
        private String rmBranchName;
        private String icCode;
        private String icName;
        private String icBranchCode;
        private String icBranchName;

        public ReferrerInput toRmInfo() {
            ReferrerInput rmInfo = new ReferrerInput();
            rmInfo.setCode(rmCode);
            rmInfo.setName(rmName);
            rmInfo.setEmail(rmEmail);
            rmInfo.setPhoneNumber(rmPhoneNumber);
            rmInfo.setBranchCode(rmBranchCode);
            rmInfo.setBranchName(rmBranchName);
            return rmInfo;
        }

        public ReferrerInput toIcInfo() {
            if (StringUtils.hasText(icCode)) {
                ReferrerInput icInfo = new ReferrerInput();
                icInfo.setCode(icCode);
                icInfo.setName(icName);
                icInfo.setBranchCode(icBranchCode);
                icInfo.setBranchName(icBranchName);
                return icInfo;
            }
            return null;
        }
    }
}
