package com.stg.repository;

import com.stg.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	@Transactional
	@Modifying
	@Query("delete from RefreshToken t where t.expiredDate < ?1")
	int deleteExpired(Instant expired);

}
