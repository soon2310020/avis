package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.UserRole;

@Repository
public interface UserRoleRepo extends JpaRepository<UserRole, Long> {
}
