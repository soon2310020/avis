package com.stg.controller;

import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.HealthQuestionService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.healthQuestion.HealthQuestionDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "APIs Câu hỏi sức khỏe và cam kết")
public class HealthQuestionController {

    private final HealthQuestionService healthQuestionService;

    @GetMapping(Endpoints.URL_HEALTH_QUESTION)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<HealthQuestionDTO> list(@AuthenticationPrincipal CustomUserDetails user) {
        return healthQuestionService.list(Long.valueOf(user.getUserId()), "4 QUESTIONS");
    }

    @GetMapping(Endpoints.URL_HEALTH_TWELVE_QUESTION)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<HealthQuestionDTO> list12Questions(@AuthenticationPrincipal CustomUserDetails user) {
        return healthQuestionService.list(Long.valueOf(user.getUserId()), "12 QUESTIONS");
    }

}
