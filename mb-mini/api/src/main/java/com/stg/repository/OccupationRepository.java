package com.stg.repository;

import com.stg.entity.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {
    @Override
    List<Occupation> findAll();
}
