package com.emoldino.api.analysis.resource.composite.bud.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.bud.dto.BudData;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetOut.BudDetails;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut.BudGraphItem;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut.BudSupplierItem;
import com.emoldino.api.analysis.resource.composite.bud.dto.QBudData;
import com.emoldino.api.analysis.resource.composite.bud.util.BudUtils;
import com.emoldino.api.analysis.resource.composite.ovrutl.util.OvrUtlUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.api.company.CompanyRepository;
import saleson.common.enumeration.CompanyType;
import saleson.model.Company;
import saleson.model.QCompany;
import saleson.model.QMold;
import saleson.model.QMoldEndLifeCycle;

@RequiredArgsConstructor
@Service
public class BudService {

	private final JPAQueryFactory queryFactory;

	public BudGetOut get(BudGetIn input, Pageable pageable) {

		QMold table = QMold.mold;
		QCompany company = QCompany.company;
		QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;

		Map<Long, Long> map = queryFactory.from(table)//
				.leftJoin(company).on(company.id.eq(table.companyId))//
				.where(wConditionSuppliers(input.getSupplierId()))//
				.groupBy(table.companyId)//
				.transform(GroupBy.groupBy(table.companyId).as(table.companyId.count()));

		List<BudData> datas = queryFactory //
				.select(new QBudData( // 
						table.id.as("moldId"), //
						table.equipmentCode.as("moldCode"), //
						table.companyId, //
						company.name.as("companyCode"), //
						table.lastShot.as("accumShotCount"), //
						table.designedShot.as("forcastMaxShot"), //
						table.cost, //
						table.salvageValue.as("salvage"), //
						table.costCurrencyType.as("costType"), moldEndLifeCycle.remainingDays //
				)) //
				.from(table) //
				.leftJoin(company).on(company.id.eq(table.companyId)) //
				.leftJoin(moldEndLifeCycle).on(moldEndLifeCycle.moldId.eq(table.id)) //
				.where(wConditionSuppliers(input.getSupplierId())) //
				.fetch();

		LocalDateTime currTime = LocalDateTime.now();
		String year = String.valueOf(currTime.getYear());
		String half = BudUtils.getCurrentHalf(currTime);

		// Grid Data	
		List<BudSupplierItem> gridItems = new ArrayList<>();

		map.forEach((key, value) -> {
			List<BudData> coDatas = datas.stream().filter(data -> data.getCompanyId().equals(key)).collect(Collectors.toList());
			if (!ObjectUtils.isEmpty(coDatas)) {
				BudSupplierItem gridItem = BudSupplierItem.builder() //
						.supplierId(key) //
						.supplierCode(coDatas.get(0).getCompanyCode()) //
						.moldCount(value) //
						.totalCost(Long.valueOf(coDatas.stream().mapToInt(data -> data.getCost().intValue()).sum())) //
						.totalSalvage(Long.valueOf(coDatas.stream().mapToInt(data -> data.getSalvage().intValue()).sum())) //					
						.costType(coDatas.get(0).getCostType()) //
						.build();
				gridItems.add(gridItem);
			}
		});

		// Graph Data
		Map<String, BudGraphItem> graphConent = new LinkedHashMap<>();
		for (int yearNo = 0; yearNo <= 5; yearNo++) {
			for (int halfNo = 1; halfNo <= 2; halfNo++) {
				String yearStr = String.valueOf(Integer.valueOf(year) + yearNo);
				String halfStr = String.valueOf(halfNo);

				List<BudSupplierItem> budSupplierItems = new ArrayList<>();
				map.forEach((key, value) -> {
					List<BudData> budData = datas.stream().filter(data -> data.getCompanyId().equals(key) && data.getYear().equals(yearStr) && data.getHalf().equals(halfStr))
							.collect(Collectors.toList());
					if (!ObjectUtils.isEmpty(budData)) {
						BudSupplierItem budSupplierItem = BudSupplierItem.builder()//
								.supplierId(key) //
								.supplierCode(budData.get(0).getCompanyCode()) //
								.moldCount(value) //
								.totalCost(Long.valueOf(budData.stream().mapToInt(data -> data.getCost().intValue()).sum())) //
								.totalSalvage(Long.valueOf(budData.stream().mapToInt(data -> data.getSalvage().intValue()).sum())) //	
								.costType(budData.get(0).getCostType()) //
								.build();
						budSupplierItems.add(budSupplierItem);
					}
				});

				if (!ObjectUtils.isEmpty(budSupplierItems)) {
					BudGraphItem budGraphItem = BudGraphItem.builder() //
							.supplierItems(budSupplierItems) //
							.totalMoldCount(budSupplierItems.stream().mapToInt(data -> data.getMoldCount().intValue()).sum()) //
							.year(yearStr) //
							.half(halfStr) //
							.build();
					graphConent.put(String.format("%s-%s", yearStr, halfStr), budGraphItem);
				} else if (ObjectUtils.isEmpty(budSupplierItems)) {
					if (yearNo == 0 && halfNo == 1)
						continue;
					BudGraphItem budGraphItem = BudGraphItem.builder() //							
							.totalMoldCount(0) //
							.year(yearStr) //
							.half(halfStr) //
							.build();
					graphConent.put(String.format("%s-%s", yearStr, halfStr), budGraphItem);
				}
			}
		}

		List<BudSupplierItem> content = sortBudViwItem(gridItems, pageable);
		final int start = (int) pageable.getOffset();
		final int end = Math.min(start + pageable.getPageSize(), content.size());

		return new BudGetOut(content.subList(start, end), pageable, end, graphConent);

	}

	public BudDetailsGetOut getDetails(BudDetailsGetIn input, Pageable pageable) {

		QMold table = QMold.mold;
		QCompany company = QCompany.company;
		QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;

		List<BudData> datas = queryFactory //
				.select(new QBudData( // 
						table.id.as("moldId"), //
						table.equipmentCode.as("moldCode"), //
						table.companyId, //
						company.name.as("companyCode"), //
						table.lastShot.as("accumShotCount"), //
						table.designedShot.as("forcastMaxShot"), //
						table.cost, //
						table.salvageValue.as("salvage"), //						
						table.costCurrencyType.as("costType"), //
						moldEndLifeCycle.remainingDays //
				)) //
				.from(table) //
				.leftJoin(company).on(company.id.eq(table.companyId)) //
				.leftJoin(moldEndLifeCycle).on(moldEndLifeCycle.moldId.eq(table.id)) //
				.where(wConditionSuppliers(input.getSupplierId())) //
				.fetch();

		List<BudDetails> content = new ArrayList<>();

		datas.forEach(data -> {
			BudDetails details = new BudDetails();
			details.setMoldId(data.getMoldId());
			details.setMoldCode(data.getMoldCode());
			details.setUtilizationRate(data.getUtilizationRate());
			details.setLifeCycleStatus(OvrUtlUtils.setLifeCycleStatus(data.getUtilizationRate()));
			details.setCost(data.getCost().longValue());
			details.setSalvage(data.getSalvage().longValue());
			details.setCostType(data.getCostType());
			content.add(details);
		});

		List<BudDetails> result = sortBudViwDetails(content, pageable);
		final int start = (int) pageable.getOffset();
		final int end = Math.min(start + pageable.getPageSize(), result.size());

		FltSupplier fltSupplier = new FltSupplier(BeanUtils.get(CompanyRepository.class)//
				.findById(input.getSupplierId())//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, "Id", input.getSupplierId())));

		return new BudDetailsGetOut(result.subList(start, end), pageable, result.size(), fltSupplier);
	}

	private BooleanExpression wConditionSuppliers(Long supplierId) {

		QMold table = QMold.mold;
		QCompany company = QCompany.company;

		BooleanExpression expression = company.companyType.eq(CompanyType.SUPPLIER);
		if (!ObjectUtils.isEmpty(supplierId)) {
			expression = expression.and(table.companyId.eq(supplierId));
		}

		return expression;
	}

	private List<BudSupplierItem> sortBudViwItem(List<BudSupplierItem> content, Pageable pageable) {
		List<BudSupplierItem> sortedResult = content;
		if (!ObjectUtils.isEmpty(pageable)) {
			for (Sort.Order order : pageable.getSort()) {
				Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
				switch (order.getProperty()) {
				case "supplierId":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getSupplierId, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getSupplierId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "supplierCode":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getSupplierCode, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getSupplierCode, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "moldCount":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getMoldCount, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getMoldCount, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "totalCost":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getTotalCost, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getTotalCost, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "totalSalvage":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getTotalSalvage, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudSupplierItem::getTotalSalvage, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				default:
					break;
				}
			}
			return sortedResult;
		}
		return null;
	}

	private List<BudDetails> sortBudViwDetails(List<BudDetails> content, Pageable pageable) {
		List<BudDetails> sortedResult = content;
		if (!ObjectUtils.isEmpty(pageable)) {
			for (Sort.Order order : pageable.getSort()) {
				Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
				switch (order.getProperty()) {
				case "moldId":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getMoldId, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getMoldId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "moldCode":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getMoldCode, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getMoldCode, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "utilizationRate":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getUtilizationRate, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getUtilizationRate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "cost":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getCost, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getCost, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "salvage":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getSalvage, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getSalvage, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				case "lifeCycleStatus":
					if (direction.equals(Order.ASC)) {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getLifeCycleStatus, Comparator.nullsLast(Comparator.naturalOrder())))
								.collect(Collectors.toList());
					} else {
						sortedResult = content.stream().sorted(Comparator.comparing(BudDetails::getLifeCycleStatus, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
								.collect(Collectors.toList());
					}
					break;
				default:
					break;
				}
			}
			return sortedResult;
		}
		return null;
	}
}
