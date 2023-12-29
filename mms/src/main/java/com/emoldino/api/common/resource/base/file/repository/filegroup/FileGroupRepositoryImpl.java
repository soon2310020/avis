package com.emoldino.api.common.resource.base.file.repository.filegroup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;

public class FileGroupRepositoryImpl extends QuerydslRepositorySupport implements FileGroupRepositoryCustom {
	public FileGroupRepositoryImpl() {
		super(FileGroup.class);
	}

	@Override
	public Page<FileGroup> findAllDistinctFileGroupTypeAndFileGroupCode(BooleanBuilder filter, Pageable pageable) {
		JPQLQuery<Tuple> query = from(Qs.fileGroup)//
				.select(//
						Qs.fileGroup.fileGroupType, //
						Qs.fileGroup.fileGroupCode//
				)//
				.distinct()//
				.where(filter);
		getQuerydsl().applyPagination(pageable, query);
		QueryResults<Tuple> results = query.fetchResults();
		List<FileGroup> content = new ArrayList<>();
		results.getResults().forEach(item -> {
			FileGroup fileGroup = BeanUtils.get(FileGroupRepository.class)//
					.findFirstByFileGroupTypeAndFileGroupCodeOrderByVersionNoDesc(//
							item.get(Qs.fileGroup.fileGroupType), //
							item.get(Qs.fileGroup.fileGroupCode))//
					.orElse(null);
			content.add(fileGroup);
		});
		return new PageImpl<>(content, pageable, results.getTotal());
	}

}
