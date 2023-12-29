package com.stg.repository;

import com.stg.entity.PavImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PavImportRepository extends JpaRepository<PavImport, Long> {

    @Query(value = "SELECT u.* FROM pav_import u " +
            "where (:packageCode = '' OR (LOWER(package_code) = CONCAT(LOWER(:packageCode)))) " +
            "and (:age = 0 OR (age = :age)) " +
            "and (:gender = '' OR (LOWER(gender) = CONCAT(LOWER(:gender)))) " +
            "and (:groupOccupationClass = 0 OR (group_occupation_class = :groupOccupationClass)) " +
            "ORDER BY u.id ASC ", nativeQuery = true)
    List<PavImport> listPav(@Param("packageCode") String packageCode,
                            @Param("age") Integer age,
                            @Param("gender") String gender,
                            @Param("groupOccupationClass") Integer groupOccupationClass);

}
