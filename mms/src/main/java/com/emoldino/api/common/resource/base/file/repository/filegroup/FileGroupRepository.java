package com.emoldino.api.common.resource.base.file.repository.filegroup;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;

public interface FileGroupRepository extends JpaRepository<FileGroup, Long>, QuerydslPredicateExecutor<FileGroup>, FileGroupRepositoryCustom {

	Optional<FileGroup> findFirstByFileGroupTypeAndFileGroupCodeOrderByVersionNoDesc(FileGroupType fileGroupType, String fileGroupCode);

	List<FileGroup> findAllByFileGroupKeyOrderByVersionNoDesc(String fileGroupKey);

}
