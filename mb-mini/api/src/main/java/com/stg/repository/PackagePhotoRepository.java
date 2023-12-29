package com.stg.repository;

import com.stg.entity.PackagePhoto;
import com.stg.service.dto.external.PackageNameEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PackagePhotoRepository extends JpaRepository<PackagePhoto, Long> {

    PackagePhoto findByType(PackageNameEnum type);
}
