package com.emoldino.api.common.resource.base.file.repository.fileitem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface FileItemRepository extends JpaRepository<FileItem, Long>, QuerydslPredicateExecutor<FileItem> {

	List<FileItem> findAllByFileGroupKeyAndParamNameOrderByPosition(String fileGroupKey, String paramName);

	List<FileItem> findAllByFileGroupKeyAndParamNameAndVersionOrderByPosition(String fileGroupKey, String paramName, String version);

}
