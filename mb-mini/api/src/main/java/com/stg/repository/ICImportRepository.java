package com.stg.repository;

import com.stg.entity.ICImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICImportRepository extends JpaRepository<ICImport, Long> {

    @Query(value = "SELECT * FROM ic_import " +
            " where status = :status " +
            " and (:query = '' " +
            "   OR (code ILIKE CONCAT('%', :query, '%')) " +
            "   OR (full_name ILIKE CONCAT('%', :query, '%'))) " +
            " ORDER BY full_name ASC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<ICImport> filterList(@Param("page") int page,
                              @Param("size") int size,
                              @Param("query") String query,
                              @Param("status") Integer status);

    @Query(value = "SELECT count(*) FROM ic_import " +
            " where status = :status " +
            " and (:query = '' " +
            "   OR (code ILIKE CONCAT('%', :query, '%')) " +
            "   OR (full_name ILIKE CONCAT('%', :query, '%'))) ", nativeQuery = true)
    long totalByQueryAndStatus(@Param("query") String query,
                              @Param("status") Integer status);

                                                  @Modifying
    @Query(value = "Update ic_import set status = 0 where 1=1", nativeQuery = true)
    void disabledAll();

    ICImport findDistinctByCode(String code);

}
