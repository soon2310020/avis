package com.emoldino.api.common.resource.composite.nothis.service;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.noti.dto.NotiGetIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.api.common.resource.base.noti.service.NotiService;
import com.emoldino.api.common.resource.composite.nothis.dto.NotHisGetIn;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.QueryResults;

import saleson.common.util.SecurityUtils;

@Service
@Transactional
public class NotHisService {

	public Page<NotiUserItem> get(NotHisGetIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		QueryResults<NotiUserItem> results;
		{
			NotiGetIn reqin = new NotiGetIn();
			if (input.getNotiCategory() != null) {
				reqin.setNotiCategory(Arrays.asList(input.getNotiCategory()));
			}
			results = BeanUtils.get(NotiService.class).getBySession(reqin, pageable);
		}

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public void read(Long id) {
		BeanUtils.get(NotiService.class).readBySession(id);
	}

}
