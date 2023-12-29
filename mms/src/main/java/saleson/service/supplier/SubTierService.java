package saleson.service.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.accessHierachy.AccessHierarchyService;
import saleson.api.company.CompanyRepository;
import saleson.api.user.UserRepository;
import saleson.model.Mold;
import saleson.model.User;

import java.util.List;

@Service
public class SubTierService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    AccessHierarchyService accessHierarchyService;
/*

    public List<User> getTierSuppliersOld(Mold mold) {
        List<User> supplierList;
        List<String> flexSubTierSuppliers = Arrays.asList("MCTRONIC", "Skreen Fabric");
        List<String> skpSubTierSuppliers = Arrays.asList("MyTech", "CGH", "TF", "GOLDPAR", "GOODHART", "SPI", "SKP BP");
        if (flexSubTierSuppliers.contains(mold.getLocation().getCompanyName())) {
            List<Company> companies = companyRepository.findByName("FLEXTRONICS");
            if (companies != null && companies.size() > 0) {
                List<Long> companyIds = companies.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
                companyIds.add(mold.getCompanyId());
                supplierList = userRepository.findByCompanyIdIn(companyIds);
            } else {
                supplierList = userRepository.findByCompanyId(mold.getCompanyId());
            }
        } else if (skpSubTierSuppliers.contains(mold.getLocation().getCompanyName())) {
            List<Company> companies = companyRepository.findByName("SKP");
            if (companies != null && companies.size() > 0) {
                List<Long> companyIds = companies.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
                companyIds.add(mold.getCompanyId());
                supplierList = userRepository.findByCompanyIdIn(companyIds);
            } else {
                supplierList = userRepository.findByCompanyId(mold.getCompanyId());
            }
        } else {
            supplierList = userRepository.findByCompanyId(mold.getCompanyId());
        }
        return supplierList;
    }
*/

    public List<User> getTierSuppliers(Mold mold) {
        List<Long> companyIds = accessHierarchyService.getFullCompanyIdAccessMold(mold);

        List<User> supplierList = userRepository.findByCompanyIdInAndDeletedIsFalse(companyIds);
        return supplierList;
    }
}
