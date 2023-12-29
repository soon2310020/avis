package com.stg.service.dto.external.responseV2;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadMultiFileQuestionRespDto {
    private String success;
}
