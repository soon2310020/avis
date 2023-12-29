package com.stg.service3rd.ocr.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrProcessReq {
    private String system;
    private String business;
    private String out_topic;
    private String user;
    private List<String> file_checksums;
    private String metadata;
}
