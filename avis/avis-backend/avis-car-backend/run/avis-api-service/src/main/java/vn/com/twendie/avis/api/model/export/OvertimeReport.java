package vn.com.twendie.avis.api.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javatuples.Pair;
import vn.com.twendie.avis.api.mapping.Mapping;
import vn.com.twendie.avis.api.mapping.WorkingDayValueMapping;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.math.BigDecimal;
import java.sql.Time;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeReport {

    private Long contractId;

    private String customerName;

    private String contractCode;

    private Long vehicleId;

    private String vehicleNumberPlate;

    private String vehicleModel;

    private BigDecimal kmNorm;

    private String vehicleOperationAdmin;

    private Long driverId;

    private String driverName;

    private long realWorkingDay;

    private Time workingTimeFrom;

    private Time workingTimeTo;

    @Mapping(WorkingDayValueMapping.class)
    private Pair<WorkingDay, Integer> workingDayValuePair;

    private BigDecimal overtime;

    private long overnight;

    private long weekend;

    private long holiday;

    private BigDecimal overtimeSurcharge;

    private BigDecimal overnightSurcharge;

    private BigDecimal weekendSurcharge;

    private BigDecimal holidaySurcharge;

}
