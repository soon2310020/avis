package com.stg.repository;

import com.stg.entity.MbEmployeeImportHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MbEmployeeImportHisRepository extends JpaRepository<MbEmployeeImportHis,Long> {
    @Query(value = "select h.* from mb_employee_import_history h order by h.created_at DESC  " +
            "OFFSET ((:page - 1) * :size) LIMIT :size ", nativeQuery = true)
    List<MbEmployeeImportHis> findAllByQuery(@Param("page") int page, @Param("size") int size);
}
