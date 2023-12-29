package saleson.api.machineDowntimeAlert.payload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import saleson.common.enumeration.MachineDowntimeAlertStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchMachineDowntimePayload {
    private String query;
    private String tab;
    private List<Long> machineIdList = Lists.newArrayList();
    private List<Long> locationIdList = Lists.newArrayList();
    private List<Long> shiftNumberList = Lists.newArrayList();
    private String fromDate;
    private String toDate;
    private Boolean lastAlert;
    private List<MachineDowntimeAlertStatus> status;
}
