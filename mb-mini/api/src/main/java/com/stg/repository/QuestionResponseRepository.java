package com.stg.repository;

import com.stg.entity.QuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, QuestionResponse.QuestionResponseId> {
    @Query(value = "select * from question_response qr where insurance_request_id = :insuranceRequestId ", nativeQuery = true)
    List<QuestionResponse> findByInsuranceRequestId(Long insuranceRequestId);
}
