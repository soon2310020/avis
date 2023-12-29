package com.stg.repository;

import com.stg.entity.MbalPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MbalPackageRepository extends JpaRepository<MbalPackage, Integer> {
    List<MbalPackage> findAllByOrderByIdAsc();
    List<MbalPackage> findAllByActiveIsTrueOrderByIdAsc();

}
