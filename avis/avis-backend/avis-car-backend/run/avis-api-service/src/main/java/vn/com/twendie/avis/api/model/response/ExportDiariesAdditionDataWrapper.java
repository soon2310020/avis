package vn.com.twendie.avis.api.model.response;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class ExportDiariesAdditionDataWrapper {
    private Timestamp diariesFrom;
    private Timestamp diariesTo;
    private String branchName;
    private String vehicleWorkingArea;
    private String contractCode;
    private String customerName;
    private String adminName;
    private String contractStatus;
    private Timestamp contractFromDate;
    private Timestamp contractToDate;
    private String contractPeriod;
    private String driverName;
    private String vehicleNumberPlate;
    private String workingTime;
    private String workingTimeHoliday;
    private DateStatistic dateStatistic;
    private List<String> nameFinds;
}
