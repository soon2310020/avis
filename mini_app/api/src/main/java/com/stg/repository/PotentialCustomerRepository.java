package com.stg.repository;

import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.search.SearchCriteria;
import com.stg.repository.search.SearchCriterias;
import com.stg.repository.search.SearchOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PotentialCustomerRepository
        extends JpaRepository<PotentialCustomer, Long>, JpaSpecificationExecutor<PotentialCustomer> {

    Optional<PotentialCustomer> findByIdAndRaw(Long id, Boolean raw);
    
    Optional<PotentialCustomer> findByIdAndRawAndCreatedBy(Long id, Boolean raw, String createdBy);

    default Page<PotentialCustomer> search(Pageable pageable, String createdBy, String query, LocalDate from, LocalDate to) {
        return search(pageable, createdBy, query, from, to, null);
    }
    
    default Page<PotentialCustomer> search(Pageable pageable, String createdBy, String query, LocalDate from, LocalDate to, PotentialCustomer potentialCustomer) {
        GenericSpesification<PotentialCustomer> specification = new GenericSpesification<>();
        specification.add(new SearchCriteria("raw", Boolean.FALSE, SearchOperation.EQUAL));
        
        if (StringUtils.hasText(createdBy)) {
            specification.add(new SearchCriteria("createdBy", createdBy, SearchOperation.EQUAL));
        }
        
        if (!ObjectUtils.isEmpty(query)) {
            List<SearchCriteria> criterias = new ArrayList<SearchCriteria>();
            criterias.add(new SearchCriteria("fullName", query, SearchOperation.MATCH));
            criterias.add(new SearchCriteria("phoneNumber", query, SearchOperation.MATCH));
            if (potentialCustomer != null) {
                criterias.add(new SearchCriteria("id", potentialCustomer.getId(), SearchOperation.EQUAL));
            }
            
            SearchCriterias searchCriterias = new SearchCriterias(criterias);
           
            specification.add(searchCriterias);
        }

        if (from != null) {
            specification.add(
                    new SearchCriteria("createdDate", from.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
        }
        if (to != null) {
            specification.add(new SearchCriteria("createdDate", to.plus(1, ChronoUnit.DAYS).atStartOfDay(),
                    SearchOperation.LESS_THAN));
        }
        return findAll(specification, pageable);
    }

    @Modifying
    @Query("update PotentialCustomer set raw = true, createdBy = '' where id in ?1 and createdBy = ?2")
    int updateToRaw(List<Long> ids, String createdBy);

    default PotentialCustomer getById(Long id, String createdBy) {
        if (StringUtils.hasText(createdBy)) {
            return findByIdAndRawAndCreatedBy(id, false, createdBy)
                    .orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH tiềm năng")));
        } else {
            return findByIdAndRaw(id, false)
                    .orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH tiềm năng")));
        }
    }
}
