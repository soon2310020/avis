package com.stg.repository;

import com.stg.entity.ImportICHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportICHistoryRepository extends JpaRepository<ImportICHistory, Long> {

    String SELECT_ALL_IC_HISTORY = "SELECT u.* FROM import_ic_history u ";
    String COUNT_ALL_IC_HISTORY = "SELECT count(*) FROM import_ic_history u ";
    String ORDER_BY_CUSTOMER_ID = "ORDER BY u.id DESC ";
    @Query(value = SELECT_ALL_IC_HISTORY + ORDER_BY_CUSTOMER_ID +
            "OFFSET (:page - 1) * :limit LIMIT :limit", nativeQuery = true)
    List<ImportICHistory> listICHistory(@Param("page") int page,
                                        @Param("limit") int limit);

    @Query(value = COUNT_ALL_IC_HISTORY, nativeQuery = true)
    long totalICHistory();

}
