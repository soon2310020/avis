package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class CostInformationDto {
    private String costOfTooling;
    private String accumulatedMaintenanceCost;
    private String salvageValue;
    private String poDate;
    private String poNumber;
    private  String memo;
}
