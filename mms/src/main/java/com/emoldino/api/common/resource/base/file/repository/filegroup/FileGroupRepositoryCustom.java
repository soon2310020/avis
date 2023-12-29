package com.emoldino.api.common.resource.base.file.repository.filegroup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;

public interface FileGroupRepositoryCustom {

	Page<FileGroup> findAllDistinctFileGroupTypeAndFileGroupCode(BooleanBuilder filter, Pageable pageable);

}
