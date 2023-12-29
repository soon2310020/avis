package com.stg.repository;

import com.stg.entity.AdditionalInsured;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AdditionalInsuredRepository extends JpaRepository<AdditionalInsured, Long> {

    @Query(value = "select * from additional_insured ai where insurance_request_id = :insuranceRequestId ", nativeQuery = true)
    List<AdditionalInsured> findByInsuranceRequestId(@Param("insuranceRequestId") Long insuranceRequestId);

    AdditionalInsured findByMiniAssuredId(String miniAssuredId);

}
