package saleson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapToolingDTO {
    private String equipmentCode;
    private OperatingStatus operatingStatus;
    private Double latitude;
    private Double longitude;
    private String locationName;

    @Override
    public String toString() {
        return "MapToolingDTO{" +
                "equipmentCode='" + equipmentCode + '\'' +
                ", operatingStatus=" + operatingStatus +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", locationName='" + locationName + '\'' +
                '}';
    }
}
