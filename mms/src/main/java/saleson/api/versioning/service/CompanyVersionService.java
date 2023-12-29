package saleson.api.versioning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.company.CompanyService;
import saleson.api.versioning.repositories.CompanyVersionRepository;
import saleson.api.versioning.repositories.ReversionHistoryRepository;
import saleson.common.enumeration.RevisionObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.clone.CompanyVersion;
import saleson.model.clone.RevisionHistory;

@Service
public class CompanyVersionService {

    @Autowired
    private CompanyVersionRepository companyVersionRepository;
    @Autowired
    private ReversionHistoryRepository reversionHistoryRepository;
    @Autowired
    private CompanyService companyService;

    private CompanyVersion clone(Company company){
        CompanyVersion companyVersion = CompanyVersion.builder()
                .companyType(company.getCompanyType())
                .companyCode(company.getCompanyCode())
                .name(company.getName())
                .address(company.getAddress())
                .manager(company.getManager())
                .phone(company.getPhone())
                .email(company.getEmail())
                .memo(company.getMemo())
                .enabled(company.isEnabled())
                .build();
        companyVersion.setOriginId(company.getId());
        return companyVersion;
    }

/*
    public Company convertToCompany(CompanyVersion companyVersion, Company company){
        if(companyVersion == null){
            return null;
        }
        if(company == null) company = new Company();
        else company.setId(companyVersion.getOriginId());

        company.setCompanyType(companyVersion.getCompanyType());
        company.setCompanyCode(companyVersion.getCompanyCode());
        company.setName(companyVersion.getName());
        company.setAddress(companyVersion.getAddress());
        company.setManager(companyVersion.getManager());
        company.setPhone(companyVersion.getPhone());
        company.setEmail(companyVersion.getEmail());
        company.setMemo(companyVersion.getMemo());
        company.setEnabled(companyVersion.isEnabled());

        return company;
    }
*/

    public void writeHistory(Company company){
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(company.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.LOCATION);
        reversionHistoryRepository.save(revisionHistory);
        companyVersionRepository.save(clone(company));
    }
/*

    public void updateAndWriteHistory(CompanyVersion companyVersion){
        if(companyVersion == null || companyVersion.getOriginId() == null) return;
        Company company = companyService.findById(companyVersion.getOriginId());
        writeHistory(company);
        companyService.save(convertToCompany(companyVersion, company));
    }
*/


}
