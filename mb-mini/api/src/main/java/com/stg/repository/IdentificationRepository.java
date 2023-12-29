package com.stg.repository;

import com.stg.entity.Identification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificationRepository extends JpaRepository<Identification, Long> {
    @Query(value = "select * from identification where customer_id = :customerId", nativeQuery = true)
    List<Identification> findByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "select * from identification where insured_mbal_id = :insuredMbalId", nativeQuery = true)
    List<Identification> findByInsuredMbalId(@Param("insuredMbalId") Long insuredMbalId);
}
