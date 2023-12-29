package saleson.api.chart.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.OperatingStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardFilterPayload {
    private List<Long> partIds;
    private List<Long> supplierIds;
    private List<Long> toolMakerIds;
    private Long startTime;
    private Long endTime;
    private List<Long> locationIds;
    private List<OperatingStatus> ops;
    private String rateCapacity;
    private EquipmentType equipmentType;
    private Long companyId;

    //for access group with tree
    @JsonIgnore
    private List<Long> accessCompanyIds=new ArrayList<>();
    @JsonIgnore
    private List<Long> accessMoldIds=new ArrayList<>();
    @JsonIgnore
    private Double mainRate;
}
