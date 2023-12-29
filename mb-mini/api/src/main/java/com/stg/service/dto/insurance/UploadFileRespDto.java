package com.stg.service.dto.insurance;

import com.stg.service.dto.upload.UploadImageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadFileRespDto {
    private List<UploadImageInfo> uploadImageInfos;
}
