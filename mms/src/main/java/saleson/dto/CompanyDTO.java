package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DataUtils;
import saleson.model.Company;

import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTO {
    private Long id;

    @JsonProperty("type")
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    @JsonProperty("code")
    private String companyCode;
    private String name;
    private String address;
    private String manager;
    private String phone;
    private String email;

    private String memo;
    private boolean enabled;

    public static CompanyDTO convertToDTO(Company company){
        CompanyDTO companyDTO = DataUtils.mapper.map(company,CompanyDTO.class);
        return companyDTO;
    }

}
