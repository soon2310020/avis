package com.stg.service.dto.campaign;

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
public class CampaignDetailDto {
    private Long id;

    private String name;

    private String event;

    private String startTime;

    private String endTime;

    private String image;

    private String content;

    private String creationTime;

    private String status;

    public void setStartTime(LocalDateTime dateTime) {
        this.startTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HM, dateTime);
    }

    public void setEndTime(LocalDateTime dateTime) {
        this.endTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HM, dateTime);
    }

    public void setCreationTime(LocalDateTime dateTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, dateTime);
    }
}
