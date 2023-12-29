package com.stg.repository;

import com.stg.entity.HealthQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthQuestionRepository extends JpaRepository<HealthQuestion, Long> {

    @Query(value = "FROM HealthQuestion WHERE type_content = :typeContent ORDER BY id asc")
    List<HealthQuestion> findByTypeContent(String typeContent);

}
