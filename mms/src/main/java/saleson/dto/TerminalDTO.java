package saleson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.util.DataUtils;
import saleson.model.Terminal;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminalDTO {
    private Long id;
    @JsonProperty("code")
    private String equipmentCode;
    @JsonProperty("purchased_date")
    private String purchasedDate;
    private String memo;

    @JsonProperty("location_id")
    private Long locationId;
    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("area")
    private String installationArea;
    @JsonProperty("installed_date")
    private String installedAt;
    @JsonProperty("installed_by")
    private String installedBy;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    private EquipmentStatus equipmentStatus;

    @JsonProperty("op_status")
    @Enumerated(EnumType.STRING)
    private OperatingStatus operatingStatus;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("ip_type")
    private IpType ipType;

    @JsonProperty("ip_address")
    private String ipAddress;
    @JsonProperty("subnet_mask")
    private String subnetMask;
    private String gateway;
    private String dns;

    public static TerminalDTO convertToDTO(Terminal terminal) {
        TerminalDTO terminalDTO = DataUtils.mapper.map(terminal, TerminalDTO.class);
        if (terminal == null) return terminalDTO;
        terminalDTO.setPurchasedDate(terminal.getPurchasedAt());
        return terminalDTO;
    }
}
