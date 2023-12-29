package com.emoldino.api.common.resource.base.code.dto;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.code.repository.codedata.QCodeData;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.Data;

@Data
public class CodeDataGetIn {
	private Long companyId;
	private String codeType;
	private String status = "Y";
	private String query;
	private String group1Code;
	private String group2Code;

	public Predicate getPredicate() {
		QCodeData table = QCodeData.codeData;
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.andIf(filter, table.codeType, codeType);
		QueryUtils.andIf(filter, table.group1Code, group1Code);
		QueryUtils.andIf(filter, table.group2Code, group2Code);
		if ("Y".equals(status)) {
			filter.and(table.enabled.isTrue());
		} else if ("N".equals(status)) {
			filter.and(table.enabled.isFalse());
		}
		filter.and(table.deleted.isFalse());
		if (!ObjectUtils.isEmpty(query)) {
			String contains = "%" + query + "%";
			filter.andAnyOf(table.code.likeIgnoreCase(contains), table.title.likeIgnoreCase(contains), table.description.likeIgnoreCase(contains));
		}
		return filter;
	}
}