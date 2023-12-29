package saleson.api.suppliersourcing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.base.product.repository.partplan.PartPlan;
import com.emoldino.api.supplychain.resource.base.product.repository.partplan.PartPlanRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.exception.SuccessException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.SupplierGetIn;
import saleson.api.suppliersourcing.payload.SupplierSourcingGetIn;
import saleson.api.suppliersourcing.payload.SupplierSourcingGetOut;
import saleson.api.suppliersourcing.payload.SupplierSourcingItem;
import saleson.api.suppliersourcing.payload.SupplierSourcingPostIn;
import saleson.model.Company;

@Transactional
@Service
public class SupplierSourcingService {

	public SupplierSourcingGetOut getList(SupplierSourcingGetIn input) {
		if (ObjectUtils.isEmpty(input.getProductId()) || ObjectUtils.isEmpty(input.getPartId()) || ObjectUtils.isEmpty(input.getPeriodValue())) {
			return new SupplierSourcingGetOut(Collections.emptyList());
		}

		long demand = ProductUtils.getPartDemandQty(input.getProductId(), input.getPartId(), input.getPeriodValue());
		long[] unfulfilled = { demand };

		List<SupplierSourcingItem> content;
		{
			Map<Long, SupplierSourcingItem> map = new LinkedHashMap<>();

			// Suppliers
			{
				SupplierGetIn reqin = new SupplierGetIn();
				reqin.setProductId(input.getProductId());
				reqin.setPartId(input.getPartId());
				reqin.setPeriodType("WEEKLY");
				reqin.setPeriodValue(input.getPeriodValue());
				BeanUtils.get(CompanyService.class).getSuppliers(reqin, PageRequest.of(0, 1000)).forEach(supplier -> {
					SupplierSourcingItem item = new SupplierSourcingItem(supplier.getId(), supplier.getName(), supplier.getMoldCount(), supplier.getTotalProduced(),
							supplier.getPredictedQuantity(), supplier.getTotalMaxCapacity());

					long remaingCapacity = 0L;
					if (supplier.getTotalMaxCapacity() > 0) {
						int compared = input.getPeriodValue().compareTo(ProductUtils.getThisWeek());
						// Future Week
						if (compared > 0) {
							remaingCapacity = supplier.getTotalMaxCapacity();
						}
						// This Week
						else if (compared == 0) {
							Calendar cal = ProductUtils.getCalendar(null);
							int days = 7 - cal.get(Calendar.DAY_OF_WEEK);
							if (days > 0) {
								remaingCapacity = supplier.getTotalMaxCapacity() - item.getTotalProduced();
							}
						}
					}

					item.setRemainingCapacity(remaingCapacity);
					map.put(supplier.getId(), item);
				});
			}

			// Plans
			{
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(Q.partPlan.productId.eq(input.getProductId()));
				filter.and(Q.partPlan.partId.eq(input.getPartId()));
				filter.and(Q.partPlan.periodType.eq("WEEKLY"));
				filter.and(Q.partPlan.periodValue.eq(input.getPeriodValue()));
				List<PartPlan> dlist = new ArrayList<>();
				BeanUtils.get(PartPlanRepository.class).findAll(filter).forEach(plan -> {
					SupplierSourcingItem item;
					// TODO Why plan.getSupplier() is null?
					Company supplier = BeanUtils.get(CompanyRepository.class).findById(plan.getSupplierId()).orElse(null);
					if (supplier == null) {
						dlist.add(plan);
						return;
					}
					if (map.containsKey(supplier.getId())) {
						item = map.get(supplier.getId());
						item.setTotalProductionDemand(plan.getQuantity());
						unfulfilled[0] = Math.max(0L, unfulfilled[0] - plan.getQuantity());
					} else {
						map.put(plan.getSupplierId(), new SupplierSourcingItem(supplier.getId(), supplier.getName(), plan.getQuantity()));
					}
				});
			}

			content = new ArrayList<>(map.values());
			for (SupplierSourcingItem item : content) {
				ProductUtils.setDeliveryRiskLevel(item);
			}
			content.sort((a, b) -> {
				if (a.getDeliverableRate() > b.getDeliverableRate()) {
					return 1;
				} else if (a.getDeliverableRate() == b.getDeliverableRate()) {
					return 0;
				} else {
					return -1;
				}
			});
		}

		SupplierSourcingGetOut output = new SupplierSourcingGetOut(content);
		output.setTotalProductionDemand(demand);
		output.setUnfulfilledDemand(unfulfilled[0]);
		return output;
	}

	public SupplierSourcingGetOut post(SupplierSourcingPostIn input) {
		LogicUtils.assertNotEmpty(input.getProductId(), "productId");
		LogicUtils.assertNotEmpty(input.getPartId(), "part");
		LogicUtils.assertNotEmpty(input.getPeriodValue(), "periodValue");

		if (!ObjectUtils.isEmpty(input.getContent())) {
			List<PartPlan> list = new ArrayList<>();
			input.getContent().forEach(item -> {
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(Q.partPlan.productId.eq(input.getProductId()));
				filter.and(Q.partPlan.partId.eq(input.getPartId()));
				filter.and(Q.partPlan.supplierId.eq(item.getId()));
				filter.and(Q.partPlan.periodType.eq("WEEKLY"));
				filter.and(Q.partPlan.periodValue.eq(input.getPeriodValue()));
				PartPlan plan = BeanUtils.get(PartPlanRepository.class).findOne(filter).orElse(null);
				if (plan == null) {
					if (item.getTotalProductionDemand() <= 0) {
						return;
					}
					plan = new PartPlan();
					plan.setProductId(input.getProductId());
					plan.setPartId(input.getPartId());
					plan.setSupplierId(item.getId());
					plan.setPeriodType("WEEKLY");
					plan.setPeriodValue(input.getPeriodValue());
					plan.setQuantity(item.getTotalProductionDemand());
				} else if (plan.getQuantity().equals(item.getTotalProductionDemand())) {
					return;
				} else {
					plan.setQuantity(item.getTotalProductionDemand());
				}
				list.add(plan);
			});
			BeanUtils.get(PartPlanRepository.class).saveAll(list);
		}

		SupplierSourcingGetIn reqin = new SupplierSourcingGetIn();
		reqin.setProductId(input.getProductId());
		reqin.setPartId(input.getPartId());
		reqin.setPeriodType(input.getPeriodType());
		reqin.setPeriodValue(input.getPeriodValue());
		return getList(reqin);
	}

	public SupplierSourcingGetOut postTemp(SupplierSourcingPostIn input) {
		try {
			TranUtils.doNewTran(() -> {
				throw new SuccessException(post(input));
			});
			return null;
		} catch (SuccessException e) {
			return (SupplierSourcingGetOut) e.getOutput();
		}
	}
}
