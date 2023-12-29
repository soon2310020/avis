package com.stg.repository;

import com.stg.entity.AdditionalProduct;
import com.stg.utils.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AdditionalProductRepository extends JpaRepository<AdditionalProduct, Long> {
    @Query(value = "select ap.* from additional_product ap " +
            "join additional_insured ai on ap.additional_insured_id = ai.id and ai.customer_is_assured = :customerIsAssured " +
            "where ai.insurance_request_id = :insuranceRequestId ", nativeQuery = true)
    List<AdditionalProduct> findByInsuranceRequestIdAndCustomer(@Param("customerIsAssured") Boolean customerIsAssured,
                                                                @Param("insuranceRequestId") Long insuranceRequestId);

    @Query(value = "select * from additional_product ap where primary_insured_id = :primaryInsuredId ", nativeQuery = true)
    List<AdditionalProduct> findByPrimaryInsuredId(@Param("primaryInsuredId") Long primaryInsuredId);

    @Query(value = "select * from additional_product ap where ap.insurance_request_id = :insuranceRequestId and ap.type = :type ", nativeQuery = true)
    List<AdditionalProduct> findByInsuranceRequestAndType( @Param("insuranceRequestId") Long insuranceRequestId,
                                                           @Param("type") String type);
}
