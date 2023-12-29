package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class BasicInformationDto {
    private String toolingId;
    private String supplierMoldCode;
    private String toolingLetter;
    private String toolingType;
    private String toolingComplexity;
    private String counterId;
    private String familyTool;
    private String forecastedMaxShots;
    private String forecastedToolLife;
    private String yeaOfToolMade;
    private String approvedCycleTime;
    private String toolDescription;

    private String engineerInCharge;
    private String plantEngineerInCharge;

}
