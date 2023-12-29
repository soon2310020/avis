package com.stg.repository;

import com.stg.entity.PavTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PavTablesRepository extends JpaRepository<PavTable, PavTable.PavTableId> {

}
