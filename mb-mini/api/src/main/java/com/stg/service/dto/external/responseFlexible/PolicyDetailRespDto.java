package com.stg.service.dto.external.responseFlexible;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PolicyDetailRespDto {

    private String policyNumber;
    private String externalAppId;
    private String status;
    private List<Coverages> coverages;


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Coverages {
        private String productCode;
        private String productName;
        private String coverageYear;
        private String startDate;
        private String endDate;
        private String sumInsured;
        private List<Insureds> insureds;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Insureds {
        private String fullName;
        private List<Identifications> identifications;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Identifications {
        @Pattern(regexp = "ZMBL02|ZMBL03|FS0002|ZMBL18|ZMBL01|ZMBL15", message = "Loại giấy tờ tùy thân")
        private String type;
        private String id;
        private String issuePlace;
        private String issueDate;
        private String expiryDate;
    }
}
