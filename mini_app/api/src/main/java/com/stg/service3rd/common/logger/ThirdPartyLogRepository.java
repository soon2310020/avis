package com.stg.service3rd.common.logger;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdPartyLogRepository extends JpaRepository<ThirdPartyLog, Long> {

}
