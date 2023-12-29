package com.stg.repository;

import com.stg.entity.combo.ComboSuggestion;
import com.stg.entity.combo.UserCombo;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.repository.search.SearchCriteria;
import com.stg.repository.search.SearchCriterias;
import com.stg.repository.search.SearchOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public interface UserComboRepository extends JpaRepository<UserCombo, Long>, JpaSpecificationExecutor<UserCombo> {

	@Modifying
	@Query("update UserCombo set raw = true where id in ?1 and createdBy = ?2 and raw = false")
	int updateToRaw(List<Long> ids, String createdBy);

	default Page<UserCombo> search(Pageable pageable, String createdBy, String query, LocalDate from, LocalDate to) {
		GenericSpesification<UserCombo> specification = new GenericSpesification<>();
		specification.add(new SearchCriteria("raw", Boolean.FALSE, SearchOperation.EQUAL));
		specification.add(new SearchCriteria("createdBy", createdBy, SearchOperation.EQUAL));
		if (!StringUtils.isEmpty(query)) {
			SearchCriterias searchCriterias = new SearchCriterias(
					List.of(new SearchCriteria("comboName", query, SearchOperation.MATCH)));
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

	Optional<UserCombo> findByIdAndCreatedBy(Long id, String createdBy);

}
