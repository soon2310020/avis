package vn.com.twendie.avis.mobile.api.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.MemberCustomer;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCustomerRepo extends JpaRepository<MemberCustomer, Long> {

    @Query(value = "SELECT * FROM member_customer c " +
            "where c.parent_id = :parent_id and lower(c.name) LIKE lower(concat('%',:name,'%')) and role in :roles"
             , nativeQuery = true)
    List<MemberCustomer> findByParentIdAndNameLikeAndRoleIn(@Param("parent_id") Long parent_id,
                                                              @Param("name") String name, @Param("roles") List<String> roles);

    List<MemberCustomer> findByCustomerId(Long customerId);


    @Query(value = "select count(*) from member_customer c where c.name = :name and lower(c.department) like lower(concat('%':department'%'))", nativeQuery = true)
    int countByNameAndDepartment(@Param("name") String name, @Param("department") String department);

    @Query(value = "SELECT MAX(code) FROM member_customer WHERE code LIKE CONCAT(?1, '%')", nativeQuery = true)
    String getNewestCodeByPrefix(String prefix);

    Optional<MemberCustomer> findByUserId(Long userId);

    @Query(nativeQuery = true, value = "select * from member_customer where user_id in :ids")
    List<MemberCustomer> findByUserIdIn(@Param("ids") List<Long> ids);

}
