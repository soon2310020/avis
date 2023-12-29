package saleson.api.rejectedPart.payload;

import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import saleson.common.enumeration.Frequent;

import java.util.List;

@Data
public class RejectRatePayload {
    private String hour;
    private String day;
    private String week;
    private String month;
    private Frequent frequent;
    private String startDate;

    private List<Long> locationIdList = Lists.newArrayList();
    private List<Long> machineIdList = Lists.newArrayList();
    private List<Long> shiftIdList = Lists.newArrayList();
}
