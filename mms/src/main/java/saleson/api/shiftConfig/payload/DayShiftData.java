package saleson.api.shiftConfig.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DayShiftType;
import saleson.model.DayShift;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayShiftData {
    private Long id;
    private Long locationId;
    private Long numberOfShifts;
    private String start;
    private String end;
    private DayShiftType dayShiftType;
    private Boolean automatic;
    private List<HourShiftData> hourShiftData = new ArrayList<>();

    public DayShiftData(DayShift dayShift) {
        this.id = dayShift.getId();
        this.locationId = dayShift.getLocationId();
        this.numberOfShifts = dayShift.getNumberOfShifts();
        this.start = dayShift.getStart();
        this.end = dayShift.getEnd();
        this.dayShiftType = dayShift.getDayShiftType();
        this.automatic = dayShift.getAutomatic();
        this.hourShiftData = dayShift.getHourShifts().stream().map(HourShiftData::new).collect(Collectors.toList());
    }

    public List<HourShiftData> getShift1() {
        return hourShiftData.stream().filter(h -> h.getShiftNumber() == 1).collect(Collectors.toList());
    }

    public List<HourShiftData> getShift2() {
        return hourShiftData.stream().filter(h -> h.getShiftNumber() == 2).collect(Collectors.toList());
    }

    public List<HourShiftData> getShift3() {
        return hourShiftData.stream().filter(h -> h.getShiftNumber() == 3).collect(Collectors.toList());
    }

    public List<HourShiftData> getShift4() {
        return hourShiftData.stream().filter(h -> h.getShiftNumber() == 4).collect(Collectors.toList());
    }
}
