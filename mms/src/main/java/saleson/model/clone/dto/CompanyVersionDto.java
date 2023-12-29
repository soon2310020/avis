package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyVersionDto {
    private Long id;
    private String companyType;
    private String companyTypeTitle;
    private String companyCode;
    private String name;
    private String address;
    private String manager;
    private String phone;
    private String email;
    private String memo;
    private boolean enabled;
}
