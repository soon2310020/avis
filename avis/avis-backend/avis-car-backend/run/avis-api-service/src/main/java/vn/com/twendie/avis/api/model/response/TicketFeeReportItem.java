package vn.com.twendie.avis.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFeeReportItem {

    private String numberPlate;
    private BigDecimal totalTireRepairFee;
    private BigDecimal totalCarWashFee;
    private BigDecimal totalTireRepairAndCarWashFee;
    private BigDecimal totalTollFee;
    private BigDecimal totalParkingFee;
    private BigDecimal totalTollAndParkingFee;
    private String accountantName;
}
