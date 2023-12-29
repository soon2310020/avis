package com.stg.repository;

import com.stg.constant.quotation.QuotationState;
import com.stg.entity.quotation.QuotationHeader;
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
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuotationHeaderRepository
		extends JpaRepository<QuotationHeader, Long>, JpaSpecificationExecutor<QuotationHeader> {

	Optional<QuotationHeader> findByIdAndRawAndCreatedBy(Long id, Boolean raw, String createdBy);

	Optional<QuotationHeader> findById(long id);

	Optional<QuotationHeader> findByUuid(UUID uuid);
	Optional<QuotationHeader> findFirstByCreatedByAndProcessIdOrderByIdDesc(String createdBy, Long processId);

	Optional<QuotationHeader> findByIdAndCreatedBy(Long id, String createdBy);

	@Query("select h from QuotationHeader h where h.raw = true and h.createdDate < ?1")
	List<QuotationHeader> findByExpiredDate(LocalDateTime expiredDate);

	@Modifying
	@Query("update QuotationHeader set raw = true, createdBy = '' where id in ?1 and createdBy = ?2")
	int updateToRaw(List<Long> ids, String createdBy);
	
	@Modifying
    @Query("update QuotationHeader set raw = true, createdBy = '' where id in ?1")
    int updateToRaw(List<Long> ids);

	@Modifying
	@Query("update QuotationHeader h set h.rawData = ?2 where h.id = ?1")
	int updateRawData(long id, String rawData);

	@Modifying
	@Query("update QuotationHeader h set h.rawData = ?2 , h.state = ?3 where h.id = ?1")
	int updateRawDataAndState(long id, String rawData, QuotationState state);

	@Query(value = "select h.raw_data\\:\\:text from quotation_header h where h.created_by = ?1 and h.process_id = ?2 order by h.id DESC limit 1", nativeQuery = true)
    String findFirstRawDataByCreatedByAndProcessId(String createdBy, Long processId);

	default QuotationHeader getById(Long id, Boolean raw, String createdBy) {
		return findByIdAndRawAndCreatedBy(id, raw, createdBy)
				.orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found")));
	}

	default QuotationHeader getById(Long id) {
		return findById(id)
				.orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found")));
	}

	default QuotationHeader getById(Long id, String createdBy) {
		return findByIdAndCreatedBy(id, createdBy)
				.orElseThrow(() -> new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found")));
	}

	default Page<QuotationHeader> search(Pageable pageable, String createdBy, String query, LocalDate date, LocalDate from, LocalDate to) {
		GenericSpesification<QuotationHeader> spesification = new GenericSpesification<>();
		spesification.add(new SearchCriteria("raw", Boolean.FALSE, SearchOperation.EQUAL));
		spesification.add(new SearchCriteria("createdBy", createdBy, SearchOperation.EQUAL));
		if (!StringUtils.isEmpty(query)) {
			SearchCriterias searchCriterias = new SearchCriterias(
					List.of(new SearchCriteria("searchName", query, SearchOperation.MATCH),
							new SearchCriteria("searchPhoneNumber", query, SearchOperation.MATCH_END)));
			spesification.add(searchCriterias);
		}
		if (date != null) {
			spesification
					.add(new SearchCriteria("createdDate", date.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
			spesification.add(new SearchCriteria("createdDate", date.plus(1, ChronoUnit.DAYS).atStartOfDay(),
					SearchOperation.LESS_THAN));
		} else {
			if (from != null) {
				spesification.add(
						new SearchCriteria("createdDate", from.atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
			}
			if (to != null) {
				spesification.add(new SearchCriteria("createdDate", to.plus(1, ChronoUnit.DAYS).atStartOfDay(),
						SearchOperation.LESS_THAN));
			}
		}
		return findAll(spesification, pageable);
	}

}
