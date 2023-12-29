package com.emoldino.api.common.resource.composite.codviw.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.common.resource.base.code.dto.CodeType;
import com.emoldino.api.common.resource.base.code.repository.codedata.CodeData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class CodViwCodeTypeDataOut extends PageImpl<CodViwCodeTypeDataItem> {
	public CodViwCodeTypeDataOut(List<CodeData> content, Pageable pageable, long total) {
		super(content.stream()
				.map(item -> new CodViwCodeTypeDataItem(item.getId(), item.getCode(), item.getTitle(), item.getDescription(), item.getCompany().getId(),
						item.getCompany().getCompanyCode(), item.getGroup1Code(), item.getGroup1Title(), item.getGroup2Code(), item.getGroup2Title()))
				.collect(Collectors.toList()), pageable, total);
	}

	private CodeType codeType;
}