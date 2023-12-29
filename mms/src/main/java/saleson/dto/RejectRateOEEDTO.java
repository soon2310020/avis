package saleson.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.math3.util.Precision;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.model.Mold;
import saleson.model.data.MiniComponentData;
import saleson.service.util.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Data
public class RejectRateOEEDTO {
    private Long id;
    private String machineCode;
    private String location;
    private Integer totalProducedAmount;
//    private Double rejectedRate;
    private Double yieldRate;
    private Integer totalRejectedAmount;
    private RejectedRateStatus rejectedRateStatus;

    private Long machineId;

    private Long locationId;

    private List<MiniComponentData> moldList = Lists.newArrayList();

    private Long entryRecord;

    @QueryProjection
    public RejectRateOEEDTO(Long id, String machineCode, String location, Integer totalProducedAmount,
                            Integer totalRejectedAmount, RejectedRateStatus rejectedRateStatus, Long machineId, Long locationId) {
        this.id = id;
        this.machineCode = machineCode;
        this.location = location;
        this.totalProducedAmount = totalProducedAmount;
        this.totalRejectedAmount = totalRejectedAmount;
        this.rejectedRateStatus = rejectedRateStatus;
        this.machineId = machineId;
        this.locationId = locationId;
    }

    public Double getYieldRate() {
        if(getRejectedRate() == null) return null;
        return 100.00 - getRejectedRate();
    }

    public Double getRejectedRate() {
        if (totalProducedAmount == 0 || totalRejectedAmount == 0) return 0D;
        double rejectRate = (double)totalRejectedAmount * 100 / totalProducedAmount;
        return NumberUtils.roundOffNumber(rejectRate);
    }
}
