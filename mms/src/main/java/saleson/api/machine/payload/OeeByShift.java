package saleson.api.machine.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.StringUtils;
import saleson.model.Mold;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OeeByShift {
    private Long hourShiftId;
    private Long shiftNumber;
    private String start;
    private String end;
    private Long partProduced;
    private Long rejectedPart;
    private Double fa;
    private Double fp;
    private Double fq;
    private Double oee;
    private String downtimeStart;
    private String downtimeEnd;
    private boolean downtime;

    private Double progress;
    private String until;
    private Instant untilInstant;

    private List<MachineMoldHistoryData> machineMoldHistoryData;

    private Double act;
    private Integer activeCavities;

    @JsonIgnore
    private Mold mold;

    private Integer downtimeDuration;
    private String hour;
    private String tenMinute;

    private List<DowntimeOeeDTO> downTimeOeeList;
    public Double getCurrentHour() {
        return StringUtils.isEmpty(until) || until.length() < 5 ? 1D : (Double.parseDouble(until.substring(3,5)) / 60);
    }

    public Double getCurrentDowntime() {
        if (downtimeDuration == null) return fa == null || fa <= 0 ? getCurrentHour() : (fa > 100 ? 0D : ( getCurrentHour() - (getCurrentHour() * (fa / 100))));
        return downtimeDuration / 3600D;
    }

    @QueryProjection
    public OeeByShift(Long partProduced, Long rejectedPart, Double fa, Double fp, Double fq, Integer downtimeDuration, String hour, String tenMinute, Mold mold) {
        this.partProduced = partProduced;
        this.rejectedPart = rejectedPart;
        this.fa = fa;
        this.fp = fp;
        this.fq = fq;
        this.downtimeDuration = downtimeDuration;
        this.hour = hour;
        this.tenMinute = tenMinute;
        this.mold = mold;
    }
}
