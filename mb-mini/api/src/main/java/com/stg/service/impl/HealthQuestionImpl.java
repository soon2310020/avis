package com.stg.service.impl;

import com.stg.entity.HealthQuestion;
import com.stg.repository.HealthQuestionRepository;
import com.stg.service.HealthQuestionService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.healthQuestion.HealthQuestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class HealthQuestionImpl implements HealthQuestionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthQuestionImpl.class);

    private final HealthQuestionRepository healthQuestionRepository;

    @Autowired
    private final ModelMapper mapper;

    @Override
    public PaginationResponse<HealthQuestionDTO> list(Long user, String typeContent) {
        PaginationResponse<HealthQuestionDTO> response = new PaginationResponse<>();
        List<HealthQuestion> healthQuestions = healthQuestionRepository.findByTypeContent(typeContent);
        List<HealthQuestionDTO> healthQuestionDTOS = new ArrayList<>();
        for (HealthQuestion healthQuestion : healthQuestions) {
            HealthQuestionDTO healthQuestionDTO = mapper.map(healthQuestion, HealthQuestionDTO.class);
            healthQuestionDTOS.add(healthQuestionDTO);
        }

        response.setData(healthQuestionDTOS);
        return response;
    }

}
