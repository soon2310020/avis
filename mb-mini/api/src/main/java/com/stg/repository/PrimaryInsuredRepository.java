package com.stg.repository;

import com.stg.entity.PrimaryInsured;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface PrimaryInsuredRepository extends JpaRepository<PrimaryInsured, Long> {

    @Query(value = "select * from primary_insured pi where insurance_request_id = :insuranceRequestId limit 1 ", nativeQuery = true)
    PrimaryInsured findByInsuranceRequestId(@Param("insuranceRequestId") Long insuranceRequestId);

}
