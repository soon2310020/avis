package com.stg.service;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

public interface RequestLogService {

    void log(HttpServletRequest request, ResponseEntity<?> response, LocalDateTime startTime, LocalDateTime endTime);

}
