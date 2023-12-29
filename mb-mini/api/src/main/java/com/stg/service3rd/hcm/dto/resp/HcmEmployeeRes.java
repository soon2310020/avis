package com.stg.service3rd.hcm.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HcmEmployeeRes {
    private String errorCode;

    private List<String> errorDesc;

    private EmployeeData data;

    public boolean hasData() {
        return data != null && data.getCount() > 0;
    }

    @Getter
    @Setter
    public static class EmployeeData {
        private int count;

        @JsonProperty("listData")
        private List<HcmEmployeeInfo> employeeList;
    }
}
