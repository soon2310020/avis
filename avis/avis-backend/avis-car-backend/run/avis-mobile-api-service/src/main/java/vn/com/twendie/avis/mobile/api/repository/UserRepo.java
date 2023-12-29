package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.User;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query(nativeQuery = true, value = "select * from user as u right join notification_setting as n on n.user_id= u.id and u.user_role_id=:roleId and n.week=true")
    List<User> findByNotificationWeekAndRole(@Param("roleId") Long roleId);

    @Query(nativeQuery = true, value = "select * from user as u inner join notification_setting as n on n.user_id= u.id and u.user_role_id=:roleId and n.month=true")
    List<User> findByNotificationMonthAndRole(@Param("roleId") Long roleId);

    @Query(nativeQuery = true, value = "select * from user where user_role_id=:roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

}
