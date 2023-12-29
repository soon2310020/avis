package com.stg.service.dto.occupation;

import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ImportOccupationHistoryDto {

    private Long id;

    private String filename;

    private Integer rowNumber;

    private String errorRow;

    private String creationTime;

    private String status;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_YYYY_MM_DD_HH_MM, creationTime);
    }

}
