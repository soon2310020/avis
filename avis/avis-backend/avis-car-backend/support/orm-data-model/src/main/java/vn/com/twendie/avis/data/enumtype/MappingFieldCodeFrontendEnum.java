package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MappingFieldCodeFrontendEnum {

    BRANCH_ID(2L, "branch_id"),
    DRIVER_ID(4L, "driver_id"),
    VEHICLE_ID(5L, "vehicle_id"),
    WORKING_DAY_ID(18L, "working_day_id"),
    WORKING_DAY(19L, "working_day"),
    WORKING_TIME_FROM(16L, "working_time_from"),
    WORKING_TIME_TO(17L, "working_time_to"),
    WORKING_TIME_WEEKEND_HOLIDAY_FROM(9L, "working_time_weekend_holiday_from"),
    WORKING_TIME_WEEKEND_HOLIDAY_TO(10L, "working_time_weekend_holiday_to"),
    NOTE(24L, "note"),
    VEHICLE_WORKING_AREA(25L, "vehicle_working_area"),
    EXTEND_STATUS(31L, "extend_status");

    private final Long id;
    private final String name;
}
