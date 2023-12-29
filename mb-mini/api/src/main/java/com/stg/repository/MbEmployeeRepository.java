package com.stg.repository;

import com.stg.entity.MbEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MbEmployeeRepository extends JpaRepository<MbEmployee,Long> {
    Optional<MbEmployee> findFirstByEmployeeCode(String employeeCode);

    @Query(value = "select e.* from mb_employee e " +
            "where (:query is null OR :query = '' OR e.employee_code ILIKE  concat('%',:query,'%') " +
            "OR e.employee_name ILIKE concat('%',:query,'%') " +
            "OR e.email ILIKE concat('%',:query,'%') " +
            "OR e.mb_id ILIKE concat('%',:query,'%') " +
            "OR e.identity_card_number ILIKE concat('%',:query,'%')) " +
            "AND (:managerUnit is null or :managerUnit = '' or e.managing_unit like :managerUnit)" +
            "ORDER BY e.updated_at DESC, e.created_at DESC " +
            "OFFSET ((:page - 1) * :size) LIMIT :size", nativeQuery = true)
    List<MbEmployee> findAllByQuery(@Param("page") int page, @Param("size") int size, @Param("query") String query, @Param("managerUnit") String managerUnit);

    @Query(value = "select count(*) from mb_employee e " +
            "where (:query is null OR :query = '' OR e.employee_code ILIKE  concat('%',:query,'%') " +
            "OR e.employee_name ILIKE concat('%',:query,'%') " +
            "OR e.email ILIKE concat('%',:query,'%') " +
            "OR e.mb_id ILIKE concat('%',:query,'%') " +
            "OR e.identity_card_number ILIKE concat('%',:query,'%')) " +
            "AND (:managerUnit is null or :managerUnit = '' or e.managing_unit like :managerUnit) " , nativeQuery = true)
   Long totalFindAllByQuery( @Param("query") String query, @Param("managerUnit") String managerUnit);

    Optional<MbEmployee> findFirstByMbId(String mbId);

    Optional<MbEmployee> findFirstByIdentityCardNumber(String ccid);


}
