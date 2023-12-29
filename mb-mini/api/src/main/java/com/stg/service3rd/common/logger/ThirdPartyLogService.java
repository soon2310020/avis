package com.stg.service3rd.common.logger;

import com.stg.service3rd.common.dto.CallerInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public interface ThirdPartyLogService {
    void log(CallerInfo callerInfo,
             HttpEntity<?> request,
             ResponseEntity<?> response,
             LocalDateTime startTime,
             LocalDateTime endTime,
             @Nullable Exception reqException);
}
