package saleson.model.data.wut;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WUTData {
    private Integer index;
    private Integer indexStartNormal;
    private Double firstNormalTimePeriod;
    private Integer firstNormalTimeShotCount;
    private String endSection;
    private SectionData warmUpTime;
    private SectionData normalTime;
    private SectionData coolDownTime;
    private SectionData downTime;
    private SectionData abnormalData;
}
