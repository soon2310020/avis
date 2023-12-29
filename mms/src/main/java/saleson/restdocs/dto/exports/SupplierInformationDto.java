package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class SupplierInformationDto {
    private String targetUptime;
    private String uptimeToleranceL1;
    private String uptimeToleranceL2;
    private String labour;
    private String supplierName;
    private String supplierCode;
    private String productionHoursPerDays;
    private String productionDayPerWeek;
    private String maximumCapacityPerWeek;
}
