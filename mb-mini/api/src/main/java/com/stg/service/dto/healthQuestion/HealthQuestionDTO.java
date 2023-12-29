package com.stg.service.dto.healthQuestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HealthQuestionDTO {
    private Long id;

    // Tiêu đề câu hỏi
    private String title;

    // Nội dung câu hỏi, nội dung checkbox cam kết
    private String content;

    // Loại: Question/Checkbox
    private String type;

    // Lựa chọn 1 cho câu hỏi
    private String option1;

    // Lựa chọn 2 cho câu hỏi
    private String option2;
}
