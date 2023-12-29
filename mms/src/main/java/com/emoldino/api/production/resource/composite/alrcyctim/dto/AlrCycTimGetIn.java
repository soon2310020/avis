package com.emoldino.api.production.resource.composite.alrcyctim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SpecialAlertType;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlrCycTimGetIn {
    private String query;
    private String filterCode;
    private String tabName;
    private SpecialAlertType specialAlertType;
    private List<Long> id;
}
