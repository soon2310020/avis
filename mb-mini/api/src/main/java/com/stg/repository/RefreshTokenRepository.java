package com.stg.repository;

import com.stg.entity.customer.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    RefreshToken findByMbIdAndAccessTokenIdsAti(String userId, String ati);
}

