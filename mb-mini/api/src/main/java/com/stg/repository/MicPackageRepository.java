package com.stg.repository;

import com.stg.entity.MicPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MicPackageRepository extends JpaRepository<MicPackage, Integer> {

    MicPackage findByName(String name);

    List<MicPackage> findAllByOrderByIdAsc();

}
