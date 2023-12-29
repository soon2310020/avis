package saleson.model.data;

import lombok.Builder;
import lombok.Data;
import saleson.model.Company;

import java.util.List;

@Data
@Builder
public class GroupCompanyData {
    List<Company> companiesInHouse;
    List<Company> companiesSupplier;
    List<Company> companiesToolMaker;
}
