package saleson.restdocs.dto.exports;

import lombok.Data;

@Data
public class PhysicalInformationDto {
    private String toolSize;
    private String toolWeight;
    private String shotSize;
    private String toolingMaker;
    private String injectionMoldingMachineID;
    private String machineTonnageQuote;
    private String machineTonnageCurrentProduction;
    private String totalCavities;
}
