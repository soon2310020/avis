package com.stg.service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ControlStateEnum {
    WAITING("Chờ đối soát"),
    IN_PROGRESS("Đang tiến hành đối soát"),
    COMPLETED("Hoàn thành đối soát");

    private String val;
}
