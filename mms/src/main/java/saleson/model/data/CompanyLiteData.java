package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Company;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLiteData {
    private Long id;
    private String companyCode;
    private String name;

    public CompanyLiteData(Company company) {
        this.id = company.getId();
        this.companyCode = company.getCompanyCode();
        this.name = company.getName();
    }
}
