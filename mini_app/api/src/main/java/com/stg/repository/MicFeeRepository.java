package com.stg.repository;

import com.stg.entity.combo.MicFee;
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

public interface MicFeeRepository extends JpaRepository<MicFee, Long> {

	@Query(value = "SELECT * FROM mic_fee where fee_package = :feePackage and fee_type in (:feeTypes) and age_range = :ageRange", nativeQuery = true)
	List<MicFee> findMicFee(String feePackage, List<String> feeTypes, Integer ageRange);

}
