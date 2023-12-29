package com.stg.repository;


import com.stg.entity.ThirdPartyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdPartyLogRepository extends JpaRepository<ThirdPartyLog, Long> {

}
