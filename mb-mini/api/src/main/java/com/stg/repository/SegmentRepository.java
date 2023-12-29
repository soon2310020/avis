package com.stg.repository;

import com.stg.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
    Segment findBySector(@Param("sector") String sector);
}
