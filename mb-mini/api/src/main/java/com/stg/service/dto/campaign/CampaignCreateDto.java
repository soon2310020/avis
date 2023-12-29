package com.stg.service.dto.campaign;

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
public class CampaignCreateDto {

    private String name;

    private String event;

    private String startTime;

    private String endTime;

    private String image;

    private String content;

}
