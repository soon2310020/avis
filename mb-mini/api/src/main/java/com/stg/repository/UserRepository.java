package com.stg.repository;

import com.stg.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u from User u  where u.email in (?1)")
    List<User> findUsersByEmailIn(Collection<String> email);

    @Query("SELECT u FROM User u " +
            "WHERE u.id = ?1")
    Optional<User> findUserById(Long id);

    @Query("SELECT u FROM User u " +
            "WHERE (?1 IS NULL OR u.id > ?1) " +
            "ORDER BY u.id")
    List<User> listUsers(Long lastId, Pageable pageable);

    @Query(value = "SELECT u.* FROM \"user\" u " +
            "where (:query = '' OR u.full_name ILIKE CONCAT('%', :query, '%')) " +
            "ORDER BY u.id DESC OFFSET (:offset - 1) * :limit LIMIT :limit", nativeQuery = true)
    List<User> listUsers(@Param("offset") int offset,
                         @Param("limit") int limit,
                         @Param("query") String query);

    @Query(value = "Select count (u.id) FROM \"user\" u " +
            "where (:query = '' OR u.full_name ILIKE CONCAT('%', :query, '%')) ", nativeQuery = true)
    long totalUsers(@Param("query") String query);

    User findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " +
            "WHERE u.role = 'SUPER_ADMIN'")
    List<User> listSuperUsers();
}
