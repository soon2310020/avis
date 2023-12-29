package com.stg.repository;

import com.stg.entity.user.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenRepository extends JpaRepository<BlackListToken, String> {
}
