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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.search.SearchCriteria;
import com.stg.repository.search.SearchCriterias;
import com.stg.repository.search.SearchOperation;

public interface PotentialCustomerDirectRepository
        extends JpaRepository<PotentialCustomerDirect, Long>, JpaSpecificationExecutor<PotentialCustomerDirect> {

    Optional<PotentialCustomerDirect> findByIdAndCreatedBy(Long id, String createdBy);

    Optional<PotentialCustomerDirect> findByIdAndRawAndCreatedBy(Long id, Boolean raw, String createdBy);

    int countByPotentialCustomerAndRaw(PotentialCustomer potentialCustomer, Boolean raw);

    Optional<PotentialCustomerDirect> findFirstByPotentialCustomerAndRawAndCifNumberNotNull(PotentialCustomer potentialCustomer, Boolean raw);

    Optional<PotentialCustomerDirect> findFirstByCifNumberAndRawIsFalse(String cif);

    default Page<PotentialCustomerDirect> search(Pageable pageable, String createdBy, String query, LocalDate from,
            LocalDate to) {
        return search(pageable, createdBy, query, from, to, null);
    }

    default Page<PotentialCustomerDirect> search(Pageable pageable, String createdBy, String query, LocalDate from,
            LocalDate to, PotentialCustomer potentialCustomer) {
        return search(pageable, createdBy, query, from, to, potentialCustomer, null, null);
    }

    default Page<PotentialCustomerDirect> search(Pageable pageable, String query, LocalDate from,
            LocalDate to, AppStatus appStatus, String leadStatus) {
        return search(pageable, null, query, from, to, null, appStatus, leadStatus);
    }

    default Page<PotentialCustomerDirect> search(Pageable pageable, String createdBy, String query, LocalDate from,
            LocalDate to, PotentialCustomer potentialCustomer, AppStatus appStatus, String leadStatus) {
        GenericSpecification<PotentialCustomerDirect> specification = new GenericSpecification<>();

        if (StringUtils.hasText(createdBy)) {
            specification.add(new SearchCriteria("createdBy", createdBy, SearchOperation.EQUAL));
        }

        specification.add(new SearchCriteria("raw", Boolean.FALSE, SearchOperation.EQUAL));
        if (!ObjectUtils.isEmpty(query)) {

            List<SearchCriteria> searchCriterias = new ArrayList<>();
            searchCriterias.add(new SearchCriteria("potentialCustomer.fullName", query, SearchOperation.MATCH));
            searchCriterias.add(new SearchCriteria("potentialCustomer.phoneNumber", query, SearchOperation.MATCH));
            searchCriterias.add(new SearchCriteria("potentialCustomer.crmId", query, SearchOperation.EQUAL));
            searchCriterias.add(new SearchCriteria("cifNumber", query, SearchOperation.EQUAL));
            searchCriterias.add(new SearchCriteria("quotationHeader.referrer.code", query, SearchOperation.EQUAL));
            searchCriterias.add(new SearchCriteria("quotationHeader.supporter.code", query, SearchOperation.EQUAL));

            if (query.startsWith(PotentialCustomerDirect.LEAD_PREFIX)) {
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

        if (from != null) {
            specification
                    .add(new SearchCriteria("createdDate", from.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
        }
        if (to != null) {
            specification.add(new SearchCriteria("createdDate", to.plus(1, ChronoUnit.DAYS).atStartOfDay(),
                    SearchOperation.LESS_THAN));
        }

        if (potentialCustomer != null) {
            specification
            .add(new SearchCriteria("potentialCustomer", potentialCustomer, SearchOperation.EQUAL));
        }

        return findAll(specification, pageable);
    }

    @Modifying
    @Query("update PotentialCustomerDirect set raw = true, createdBy = '' where id in ?1 and createdBy = ?2")
    int updateToRaw(List<Long> ids, String createdBy);

    @Query("select d.quotationHeader.id from PotentialCustomerDirect d where id in ?1")
    List<Long> getQuotationIdByDirectIds(List<Long> ids);

    @Query("select d.userCombo.id from PotentialCustomerDirect d where id in ?1")
    List<Long> getUserComboIdByDirectIds(List<Long> ids);

    default PotentialCustomerDirect getById(Long id, String createdBy) {
        if (StringUtils.hasText(createdBy)) {
            return findByIdAndCreatedBy(id, createdBy)
                    .orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH direct")));
        } else {
            return findById(id)
                    .orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH direct")));
        }
    }

    @Query("select d from PotentialCustomerDirect d where d.quotationHeader is not null and d.submitStatus.status = ?1 and d.submitStatus.retry <= ?2")
    Page<PotentialCustomerDirect> findNextRetry(DirectSubmitStatus status, int maxRetry, Pageable pageable);

}
