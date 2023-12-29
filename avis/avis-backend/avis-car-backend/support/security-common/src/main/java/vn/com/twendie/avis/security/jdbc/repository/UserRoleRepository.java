package vn.com.twendie.avis.security.jdbc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.com.twendie.avis.data.model.UserRole;

@Repository("userRoleRepositoryInSecurity")
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
