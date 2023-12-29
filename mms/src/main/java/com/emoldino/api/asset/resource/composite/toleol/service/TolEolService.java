package com.emoldino.api.asset.resource.composite.toleol.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetIn;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetOut;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetOut.TolEolChartItem;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolItem;
import com.emoldino.api.asset.resource.composite.toleol.repository.TolEolRepository;
import com.emoldino.api.common.resource.base.masterdata.dto.Currency;
import com.emoldino.api.common.resource.base.masterdata.util.MasterDataUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;

@Service
@Transactional
public class TolEolService {

	public TolEolGetOut get(TolEolGetIn input, Pageable pageable) {
		List<TolEolChartItem> chartItems = new ArrayList<>();
		// Chart Items
		if (ObjectUtils.isEmpty(input.getTimeValue())) {
			Instant instant = DateUtils2.getInstant();
			int i = 0;
			do {
				String half = DateUtils2.toHalfByMonth(DateUtils2.format(instant, DatePattern.yyyyMM, Zone.GMT));
				instant = DateUtils2.plusMonths(instant, 6, Zone.GMT);

				i++;

				TolEolGetIn reqin = ValueUtils.map(input, TolEolGetIn.class);
				reqin.setTimeScale(TimeScale.HALF);
				reqin.setTimeValue(half);

				long count = BeanUtils.get(TolEolRepository.class).count(reqin);
				chartItems.add(new TolEolChartItem(half, count));
			} while (i < 10);
//			Pair<String, String> dateRange = DateUtils2.toDateRange(input);
		}

		Page<TolEolItem> page = BeanUtils.get(TolEolRepository.class).findAll(input, pageable);

		Currency currency = MasterDataUtils.getMainCurrency();
		double rate = currency.getExchangeRate();
		String currencyCode = currency.getCurrencyType().name();
		String currencySymbol = currency.getCurrencyType().getTitle();

		page.getContent().forEach(item -> {
			FltPartIn reqin = ValueUtils.map(input, FltPartIn.class);
			reqin.setMoldId(Arrays.asList(item.getId()));
			item.setParts(BeanUtils.get(FltService.class).getParts(reqin, PageRequest.of(0, 50)).getContent());

			item.setCost(calcCost(item.getCost(), rate));
			item.setSalvageValue(calcCost(item.getSalvageValue(), rate));
			item.setCurrencyCode(currencyCode);
			item.setCurrencySymbol(currencySymbol);
		});

		TolEolGetOut output = new TolEolGetOut(page.getContent(), page.getPageable(), page.getTotalElements(), chartItems);
		return output;
	}

	private static BigDecimal calcCost(BigDecimal cost, double rate) {
		if (cost == null) {
			return null;
		} else if (rate == 0d) {
			return cost;
		}
		double dcost = cost.doubleValue();
		if (dcost == 0d) {
			return cost;
		}
		long lcost = ValueUtils.toLong(dcost / rate, 0L);
		return new BigDecimal(lcost);
	}

}
