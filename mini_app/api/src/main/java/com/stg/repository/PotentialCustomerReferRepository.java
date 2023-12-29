package com.stg.repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.potentialcustomer.PotentialCustomerRefer;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.search.SearchCriteria;
import com.stg.repository.search.SearchCriterias;
import com.stg.repository.search.SearchOperation;

public interface PotentialCustomerReferRepository
        extends JpaRepository<PotentialCustomerRefer, Long>, JpaSpecificationExecutor<PotentialCustomerRefer> {

    Optional<PotentialCustomerRefer> findByIdAndCreatedBy(Long id, String createdBy);

    int countByPotentialCustomer(PotentialCustomer potentialCustomer);
    
    default Page<PotentialCustomerRefer> search(Pageable pageable, String createdBy, String query, LocalDate date,
            LocalDate from, LocalDate to) {
        return search(pageable, createdBy, query, date, from, to, null);
    }
    
    default Page<PotentialCustomerRefer> search(Pageable pageable, String createdBy, String query, LocalDate date,
            LocalDate from, LocalDate to, PotentialCustomer potentialCustomer) {
        return search(pageable, createdBy, query, date, from, to, potentialCustomer, null,  null);
    }
    
    
    default Page<PotentialCustomerRefer> search(Pageable pageable, String createdBy, String query, LocalDate date,
            LocalDate from, LocalDate to, AppStatus appStatus, String leadStatus) {
        return search(pageable, createdBy, query, date, from, to, null, appStatus,  leadStatus);
    }
    
    default Page<PotentialCustomerRefer> search(Pageable pageable, String createdBy, String query, LocalDate date,
            LocalDate from, LocalDate to, PotentialCustomer potentialCustomer, AppStatus appStatus, String leadStatus) {
        GenericSpesification<PotentialCustomerRefer> specification = new GenericSpesification<>();
        
        if (StringUtils.hasText(createdBy)) {
            specification.add(new SearchCriteria("createdBy", createdBy, SearchOperation.EQUAL));
        }
        
        if (StringUtils.hasLength(query)) {

            List<SearchCriteria> searchCriterias = new ArrayList<>();
            searchCriterias.add(new SearchCriteria("potentialCustomer.fullName", query, SearchOperation.MATCH));
            searchCriterias.add(new SearchCriteria("potentialCustomer.phoneNumber", query, SearchOperation.MATCH));
            searchCriterias.add(new SearchCriteria("rmCode", query, SearchOperation.EQUAL));
            searchCriterias.add(new SearchCriteria("icCode", query, SearchOperation.EQUAL));

            if (query.startsWith(PotentialCustomerRefer.LEAD_PREFIX)) {
                try {
                    Long id = Long.parseLong(query.substring(PotentialCustomerDirect.LEAD_PREFIX.length()));
                    searchCriterias.add(new SearchCriteria("id", id, SearchOperation.EQUAL));
                } catch (NumberFormatException e) {
                }
            }

            specification.add(new SearchCriterias(searchCriterias));

        }
        
        if (appStatus != null) {
            specification.add(new SearchCriteria("appStatus", appStatus, SearchOperation.EQUAL));
        }
        
        if (StringUtils.hasLength(leadStatus)) {
            if ("0".equals(leadStatus)) {
                specification.add(new SearchCriteria("leadInfo", null, SearchOperation.EQUAL));
            } else {
                specification.add(new SearchCriteria("leadInfo.status", leadStatus, SearchOperation.EQUAL));
            }
        }


        if (date != null) {
            specification
                    .add(new SearchCriteria("createdDate", date.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
            specification.add(new SearchCriteria("createdDate", date.plus(1, ChronoUnit.DAYS).atStartOfDay(),
                    SearchOperation.LESS_THAN));
        } else {
            if (from != null) {
                specification.add(
                        new SearchCriteria("createdDate", from.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
            }
            if (to != null) {
                specification.add(new SearchCriteria("createdDate", to.plus(1, ChronoUnit.DAYS).atStartOfDay(),
                        SearchOperation.LESS_THAN));
            }
        }

        if (potentialCustomer != null) {
            specification
            .add(new SearchCriteria("potentialCustomer", potentialCustomer, SearchOperation.EQUAL));
        }
        
        return findAll(specification, pageable);
    }

    default PotentialCustomerRefer getById(Long id, String createdBy) {
        if (StringUtils.hasText(createdBy)) {
            return findByIdAndCreatedBy(id, createdBy)
                    .orElseThrow(() -> new ApplicationException("",
                            new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH refer")));
        } else {
            return findById(id)
                    .orElseThrow(() -> new ApplicationException("",
                            new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH refer")));
        }
    }

}
