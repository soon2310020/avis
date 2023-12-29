package com.stg.repository;

import com.stg.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    @Query(value = "select * from beneficiary b where insurance_request_id = :insuranceRequestId ", nativeQuery = true)
    List<Beneficiary> findByInsuranceRequestId(@Param("insuranceRequestId") Long insuranceRequestId);

}
