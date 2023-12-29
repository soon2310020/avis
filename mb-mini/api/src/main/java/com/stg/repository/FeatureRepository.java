package com.stg.repository;

import com.stg.entity.user.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeatureRepository extends JpaRepository<Feature, Integer> {

    @Modifying
    @Query("update Feature fu set fu.enabled = true where fu.id in ?1")
    void enabled(List<Integer> ids);

    @Modifying
    @Query("update Feature fu set fu.enabled = false")
    void disabledAll();

    List<Feature> findByIdIn(List<Integer> Ids);

    List<Feature> getAllByEnabled(Boolean enabled);
}
