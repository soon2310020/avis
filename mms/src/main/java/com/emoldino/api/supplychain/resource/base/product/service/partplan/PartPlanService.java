package com.emoldino.api.supplychain.resource.base.product.service.partplan;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.base.product.dto.PartPlanYearly;
import com.emoldino.api.supplychain.resource.base.product.dto.PartSupplier;
import com.emoldino.api.supplychain.resource.base.product.repository.partplan.PartPlan;
import com.emoldino.api.supplychain.resource.base.product.repository.partplan.PartPlanRepository;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.part.PartRepository;
import saleson.model.Part;

@Service
public class PartPlanService {

	public List<PartPlanYearly> getYearly(List<PartSupplier> partSuppliers, String year) {
		if (ObjectUtils.isEmpty(partSuppliers)) {
			return Collections.emptyList();
		}
		int finalWeek = DateUtils2.getFinalWeekNo(year);

		List<PartPlanYearly> list = partSuppliers.stream()//
				.map(partSupplier -> {
					PartPlanYearly planYearly = BeanUtils.get(PartPlanService.class).getYearly(partSupplier, year, finalWeek);
					return planYearly;
				})//
				.collect(Collectors.toList());
		return list;
	}

	private PartPlanYearly getYearly(PartSupplier partSupplier, String year, int finalWeek) {
		validate(partSupplier);

		Map<String, PartPlan> plans = BeanUtils.get(PartPlanService.class).getByYear(partSupplier, year);
		PartPlanYearly planYearly = new PartPlanYearly();
		planYearly.setPartId(partSupplier.getPartId());
		planYearly.setPartName(partSupplier.getPartName());
		planYearly.setPartCode(partSupplier.getPartCode());
		planYearly.setSupplierId(partSupplier.getSupplierId());
		planYearly.setSupplierName(partSupplier.getSupplierName());
		planYearly.setSupplierCode(partSupplier.getSupplierCode());
		for (int i = 1; i <= finalWeek; i++) {
			String week = year + ValueUtils.pad(i, 2, "left", "0");
			Long weeklyDemand = plans.containsKey(week) ? ValueUtils.toLong(plans.get(week).getQuantity(), 0L) : 0L;
			try {
				ReflectionUtils.getSetter(planYearly, "w" + i).invoke(planYearly, weeklyDemand);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return planYearly;
	}

	private void validate(PartSupplier partSupplier) {
		LogicUtils.assertNotNull(partSupplier, "partSupplier");
		LogicUtils.assertNotNull(partSupplier.getPartId(), "partId");
		LogicUtils.assertNotNull(partSupplier.getSupplierId(), "supplierId");
	}

	private Map<String, PartPlan> getByYear(PartSupplier partSupplier, String year) {
		validate(partSupplier);

		Long productId = partSupplier.getProductId();

		Map<String, PartPlan> plans = new LinkedHashMap<>();
		BeanUtils.get(PartPlanRepository.class).findAll(new BooleanBuilder()//
				.and(productId == null ? Q.partPlan.productId.isNull() : Q.partPlan.productId.eq(productId))//
				.and(Q.partPlan.partId.eq(partSupplier.getPartId()))//
				.and(Q.partPlan.supplierId.eq(partSupplier.getSupplierId()))//
				.and(Q.partPlan.periodType.eq("WEEKLY"))//
				.and(Q.partPlan.periodValue.startsWith(year))//
				.and(Q.partPlan.quantity.isNotNull())//
		).forEach(item -> plans.put(item.getPeriodValue(), item));
		return plans;
	}

	public void saveYearly(List<PartPlanYearly> content, String year) {
		if (ObjectUtils.isEmpty(content)) {
			return;
		}

		int finalWeek = DateUtils2.getFinalWeekNo(year);

		content.forEach(planYearly -> {
			for (int i = 1; i <= finalWeek; i++) {
				String week = year + ValueUtils.pad(i, 2, "left", "0");
				Long weeklyDemand;
				try {
					weeklyDemand = (Long) ReflectionUtils.getGetter(planYearly, "w" + i).invoke(planYearly);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (weeklyDemand != null) {
					BeanUtils.get(PartPlanService.class).saveByWeek(planYearly.getPartId(), planYearly.getSupplierId(), week, weeklyDemand);
				}
			}
		});
	}

	public void saveByWeek(Long partId, Long supplierId, String week, Long quantity) {
		if (partId == null || supplierId == null || ObjectUtils.isEmpty(week) || quantity == null) {
			return;
		}
		Part part = getPart(partId);
		if (part == null) {
			return;
		}
		Long productId = part.getCategoryId();

		PartPlan partPlan = BeanUtils.get(PartPlanRepository.class).findOne(new BooleanBuilder()//
				.and(productId == null ? Q.partPlan.productId.isNull() : Q.partPlan.productId.eq(productId))//
				.and(Q.partPlan.partId.eq(partId))//
				.and(Q.partPlan.supplierId.eq(supplierId))//
				.and(Q.partPlan.periodType.eq("WEEKLY"))//
				.and(Q.partPlan.periodValue.eq(week))//
		).orElse(null);
		if (partPlan == null) {
			partPlan = new PartPlan();
			partPlan.setProductId(productId);
			partPlan.setPartId(partId);
			partPlan.setSupplierId(supplierId);
			partPlan.setPeriodType("WEEKLY");
			partPlan.setPeriodValue(week);
		} else if (quantity.equals(partPlan.getQuantity())) {
			return;
		}
		partPlan.setQuantity(quantity);
		BeanUtils.get(PartPlanRepository.class).save(partPlan);
	}

	private Part getPart(Long partId) {
		Part part = ThreadUtils.getProp("PartPlanService.part." + partId, () -> BeanUtils.get(PartRepository.class).findById(partId).orElse(null));
		return part;
	}

}
