package com.stg.service.dto.upload;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageInfo {
    private String url;
    private HttpStatus status = HttpStatus.OK;
    private int code = status.value();
    private String message;
}
