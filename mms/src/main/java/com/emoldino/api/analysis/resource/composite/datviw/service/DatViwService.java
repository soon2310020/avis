package com.emoldino.api.analysis.resource.composite.datviw.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageIn;
import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageItem;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.querydsl.core.BooleanBuilder;

@Service
public class DatViwService {

	public Page<DatColGetPageItem> getPage(DatColGetPageIn input, Pageable pageable) {
//		QData3Collected table = QData3Collected.data3Collected;
		BooleanBuilder filter = new BooleanBuilder();
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.DESC, "requestId", "position");
		}
		Page<Data3Collected> page = BeanUtils.get(Data3CollectedRepository.class).findAll(filter, pageable);
		List<DatColGetPageItem> content = new ArrayList<>();
		page.forEach(data -> {
			DatColGetPageItem item = new DatColGetPageItem();
			item.setId(data.getId());
			item.setRequestId(data.getRequestId());
			item.setPosition(data.getPosition());
			item.setProcStatus(data.getProcStatus());
			item.setProcErrorId(data.getProcErrorId());

			item.setOccurredAtStr(DateUtils2.format(data.getOccurredAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
			item.setSentAtStr(DateUtils2.format(data.getSentAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
			item.setDistributedAtStr(DateUtils2.format(data.getDistributedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
			item.setCreatedAtStr(DateUtils2.format(data.getCreatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
			item.setUpdatedAtStr(DateUtils2.format(data.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));

			item.setDataType(data.getDataType());
			item.setData(data.getData());
			item.setDeviceId(data.getDeviceId());
			item.setDeviceType(data.getDeviceType());
			item.setDeviceSwVersion(data.getDeviceSwVersion());
			item.setBrokerId(data.getBrokerId());
			item.setBrokerType(data.getBrokerType());
			item.setBrokerSwVersion(data.getBrokerSwVersion());
			content.add(item);
		});
		return new PageImpl<DatColGetPageItem>(content, page.getPageable(), page.getTotalElements());
	}

}
