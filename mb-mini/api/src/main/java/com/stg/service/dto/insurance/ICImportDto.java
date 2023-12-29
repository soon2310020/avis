package com.stg.service.dto.insurance;

import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ICImportDto {

    private Long id;

    private String code;

    private String fullName;

    private String standardContract;

    private Integer status;

    private String creationTime;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, creationTime);
    }

}
