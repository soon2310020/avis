package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Company;

@Data
public class CompanyData {
    private Long id;
    private Company company;

    @QueryProjection
    public CompanyData(Long id, Company company) {
        this.id = id;
        this.company = company;
    }
}
