package vn.com.twendie.avis.security.jdbc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(String username);

	boolean existsByUsername(String username);

}
