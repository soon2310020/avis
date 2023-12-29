package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DataUtils;
import saleson.model.Company;
import saleson.model.Location;

import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {

    private Long id;

    @JsonProperty("company_id")
    private Long companyId;
    @JsonProperty("country_code")
    private String countryCode;
//    @JsonProperty("company_code")
//    private String companyCode;
    @JsonProperty("company_name")
    private String companyName;

    private String name;
    @JsonProperty("code")
    private String locationCode;
    private String address;
    private String memo;
    private boolean enabled;

    private Double latitude;
    private Double longitude;

    public static LocationDTO convertToDTO(Location location){
        LocationDTO locationDTO = DataUtils.mapper.map(location,LocationDTO.class);
        return locationDTO;
    }
}
