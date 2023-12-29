package com.emoldino.api.analysis.resource.base.data.service.data3;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;

@Service
public class Data3Service {

	public void postList(List<Data3Collected> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}

		list.forEach(item -> {
			// Validation
			LogicUtils.assertNull(item.getId(), "id");

			// Populate
			DataUtils.populate4Insert(item);
			if (ObjectUtils.isEmpty(item.getProcStatus())) {
				item.setProcStatus("CREATED");
			}
		});

		BeanUtils.get(Data3CollectedRepository.class).saveAll(list);
	}

	public void migrateDuplicated() {
		Page<Data3Collected> page;
		int counter = 0;
		while (counter++ < 1000 //
				&& !(page = TranUtils.doNewTran(() -> BeanUtils.get(Data3CollectedRepository.class).findAll(//
						new BooleanBuilder().and(QData3Collected.data3Collected.procStatus.eq("DUPLICATED")), //
						PageRequest.of(0, 100, Direction.ASC, "id")//
				))).isEmpty()) {
			page.forEach(item -> {
				item.setProcStatus("RECREATED");
				TranUtils.doNewTran(() -> BeanUtils.get(Data3CollectedRepository.class).save(item));
			});
		}
	}

}
