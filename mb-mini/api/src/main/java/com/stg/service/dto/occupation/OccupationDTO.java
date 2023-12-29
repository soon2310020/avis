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
public class OccupationDTO {
    private Long id;
    private Integer no;
    private String job;

    private String occupation;

    private String occupationGroup;

    private String column1;

    private String column2;

    private String shorten;

    private String column3;

    private String creationTime;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, creationTime);
    }
}
