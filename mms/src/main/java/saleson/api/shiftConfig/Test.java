package saleson.api.shiftConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import saleson.api.shiftConfig.payload.DayShiftData;
import saleson.api.shiftConfig.payload.HourShiftData;
import saleson.api.shiftConfig.payload.ShiftConfigData;
import saleson.common.enumeration.DayShiftType;

import java.time.Instant;
import java.util.Collections;

public class Test {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ShiftConfigData data = new ShiftConfigData();
        data.setLocationId(73061L);
        data.setLocationCode("SK - 01");
        data.setLocationName("Seoul");

        DayShiftData defaultData = new DayShiftData();
        defaultData.builder()
                .locationId(73061L)
                .dayShiftType(DayShiftType.DEFAULT)
                .numberOfShifts(4L)
                .start("0600")
                .end("0600")
                .automatic(true)
                .build();

        defaultData.getHourShiftData().add(new HourShiftData(1L, "0600", "0700"));
        defaultData.getHourShiftData().add(new HourShiftData(1L, "0700", "0800"));
        defaultData.getHourShiftData().add(new HourShiftData(1L, "0800", "0900"));
        defaultData.getHourShiftData().add(new HourShiftData(1L, "0900", "1000"));
        defaultData.getHourShiftData().add(new HourShiftData(1L, "1000", "1100"));
        defaultData.getHourShiftData().add(new HourShiftData(1L, "1100", "1200"));

        defaultData.getHourShiftData().add(new HourShiftData(2L, "1200", "1300"));
        defaultData.getHourShiftData().add(new HourShiftData(2L, "1300", "1400"));
        defaultData.getHourShiftData().add(new HourShiftData(2L, "1400", "1500"));
        defaultData.getHourShiftData().add(new HourShiftData(2L, "1500", "1600"));
        defaultData.getHourShiftData().add(new HourShiftData(2L, "1600", "1700"));
        defaultData.getHourShiftData().add(new HourShiftData(2L, "1700", "1800"));

        defaultData.getHourShiftData().add(new HourShiftData(3L, "1800", "1900"));
        defaultData.getHourShiftData().add(new HourShiftData(3L, "1900", "2000"));
        defaultData.getHourShiftData().add(new HourShiftData(3L, "2000", "2100"));
        defaultData.getHourShiftData().add(new HourShiftData(3L, "2100", "2200"));
        defaultData.getHourShiftData().add(new HourShiftData(3L, "2200", "2300"));
        defaultData.getHourShiftData().add(new HourShiftData(3L, "2300", "0000"));

        defaultData.getHourShiftData().add(new HourShiftData(4L, "0000", "0100"));
        defaultData.getHourShiftData().add(new HourShiftData(4L, "0100", "0200"));
        defaultData.getHourShiftData().add(new HourShiftData(4L, "0200", "0300"));
        defaultData.getHourShiftData().add(new HourShiftData(4L, "0300", "0400"));
        defaultData.getHourShiftData().add(new HourShiftData(4L, "0400", "0500"));
        defaultData.getHourShiftData().add(new HourShiftData(4L, "0500", "0600"));

        data.setDayShiftData(Collections.singletonList(defaultData));

        System.out.printf(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }
}
