package com.stg.repository;

import com.stg.entity.user.FeatureUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeatureUserRepository extends JpaRepository<FeatureUser, Long> {

    @Modifying
    @Query("update FeatureUser fu set fu.feature = ?2 where fu.user.id  not in ?1")
    void updateByUserIds(List<Long> userIds, String features);

    FeatureUser findByUserId(Long userId);

}
