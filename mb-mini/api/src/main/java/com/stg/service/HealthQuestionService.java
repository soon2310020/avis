package com.stg.service;

import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.healthQuestion.HealthQuestionDTO;

public interface HealthQuestionService {

    PaginationResponse<HealthQuestionDTO> list(Long user, String typeContent);
}
