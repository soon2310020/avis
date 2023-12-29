package com.emoldino.api.analysis.resource.composite.proana.service.part;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaPartsGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaPartsItem;
import com.emoldino.api.analysis.resource.composite.proana.repository.ProAnaRepository;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

@Service
@Transactional
public class ProAnaPartService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.HOUR, TimeScale.DATE);

	public ListOut<ProAnaPartsItem> get(ProAnaPartsGetIn input) {
		ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		String day;
		if (TimeScale.HOUR.equals(input.getTimeScale())) {
			day = input.getTimeValue().substring(0, 8);
		} else if (TimeScale.DATE.equals(input.getTimeScale())) {
			day = input.getTimeValue();
		} else {
			return new ListOut<>(Collections.emptyList());
		}

		List<ProAnaPartsItem> parts = BeanUtils.get(ProAnaRepository.class).findAllPartsByMoldAndDay(input.getMoldId(), day);

		return new ListOut<>(parts);
	}

}
