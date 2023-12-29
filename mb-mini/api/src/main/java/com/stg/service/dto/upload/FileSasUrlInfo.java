package com.stg.service.dto.upload;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileSasUrlInfo {
    private String url;
}
