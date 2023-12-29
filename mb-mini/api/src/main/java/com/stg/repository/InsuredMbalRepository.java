package com.stg.repository;

import com.stg.entity.InsuredMbal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuredMbalRepository extends JpaRepository<InsuredMbal, Long> {
    @Query(value = "select * from insured_mbal im where im.product_mbal_id = :productMbalId", nativeQuery = true)
    InsuredMbal findByInsuranceContractId(@Param("productMbalId") Long productMbalId);

}
