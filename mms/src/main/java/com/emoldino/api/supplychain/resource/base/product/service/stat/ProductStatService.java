package com.emoldino.api.supplychain.resource.base.product.service.stat;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductStatCleanBatchIn;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStat;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStatRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStat;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStatRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.QProductStat;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils.CapaSummary;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.repository.cachedata.CacheDataRepository;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.model.Category;
import saleson.model.Mold;
import saleson.model.Part;

@Service
public class ProductStatService {

	@Transactional(propagation = Propagation.NEVER)
	public void summarizeBatch() {
		JobUtils.runIfNotRunning("sc.prod.summarizeBatch", new JobOptions().setClustered(true), () -> {
			summarizePartBatch();
			summarizeProductBatch();
		});
	}

	private void summarizeProductBatch() {
		DataUtils.runBatch(CategoryRepository.class, //
				QueryUtils.isProduct(Q.category), Sort.by("id"), 10, true, //
				product -> summarizeProductBatch(product)//
		);
	}

	private void summarizeProductBatch(Category product) {
		if (product.getLevel() == null || product.getLevel() != 3) {
			return;
		}

		String[] day = { getLastProdStatDay(product.getId()) };

		long[] weeklyDemand = { 0L };
		long[] dailyDemand = { 0L };

		String today = DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyyMMdd, Zone.SYS);
		Set<String> demandSaved = new HashSet<>();
		do {
			String week = DateUtils2.toOtherPattern(day[0], DatePattern.yyyyMMdd, DatePattern.YYYYww);
			String year = day[0].substring(0, 4);
			String month = day[0].substring(0, 6);

			if (!demandSaved.contains(week)) {
				weeklyDemand[0] = TranUtils.doNewTran(() -> ProductUtils.getDemandQty(product.getId(), week));
				dailyDemand[0] = weeklyDemand[0] > 0L ? weeklyDemand[0] / 7 : 0L;
				TranUtils.doNewTran(() -> ProductUtils.saveDemand(product.getId(), week, weeklyDemand[0]));
			}

			@SuppressWarnings("unchecked")
			Map<Long, Map<String, Long>> invs = TranUtils.doNewTran(() -> CacheDataUtils.get("ProductStat.inv", product.getId().toString(), Map.class));
			if (invs == null) {
				invs = new LinkedHashMap<>();
			}

			ProductStat data = new ProductStat();
			data.setProductId(product.getId());
			data.setWeek(week);
			data.setYear(year);
			data.setMonth(month);
			data.setDay(day[0]);
			Map<Long, Map<String, Long>> _invs = invs;
			List<Part> parts = TranUtils.doNewTran(() -> ProductUtils.getProducableParts(product.getId(), day[0]));
			Long[] capacity = { null };
			parts.forEach(part -> {
				PartSummary partSummary = TranUtils.doNewTran(() -> getPartSummary(product, part, week, year, month, day[0]));
				if (partSummary == null) {
					return;
				}

				long rq = ValueUtils.toLong(part.getQuantityRequired(), 0L);

				if (partSummary.getProduced() > 0 || partSummary.getProducedVal() > 0) {
					Map<String, Long> map;
					if (_invs.containsKey(part.getId())) {
						map = _invs.get(part.getId());
					} else {
						map = new LinkedHashMap<String, Long>();
					}
					map.put("produced", ValueUtils.toLong(map.get("produced"), 0L) + partSummary.getProduced());
					map.put("producedVal", ValueUtils.toLong(map.get("producedVal"), 0L) + partSummary.getProducedVal());
					_invs.put(part.getId(), map);
				}
				if (rq > 0L) {
					long totalCapa = partSummary.getTotalCapacity();
					if (totalCapa <= 0L) {
						capacity[0] = 0L;
					} else if (capacity[0] == null) {
						capacity[0] = totalCapa / rq;
					} else {
						capacity[0] = Math.min(capacity[0], totalCapa / rq);
					}
				}
			});
			data.setDailyCapacity(ValueUtils.toLong(capacity[0], 0L));
			data.setPartCount(BeanUtils.get(PartRepository.class).countByProduct(product.getId(), null, null).intValue());
			data.setSupplierCount(BeanUtils.get(CompanyRepository.class).countByProduct(product.getId(), null, null).intValue());
			data.setMoldCount(BeanUtils.get(MoldRepository.class).countByProduct(product.getId(), null, null).intValue());

			while (consume(data, parts, _invs)) {

			}

			TranUtils.doNewTran(() -> {
				ProductStat prodStat;
				{
					QProductStat table = QProductStat.productStat;
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.productId.eq(product.getId()));
					filter.and(table.day.eq(day[0]));
					prodStat = BeanUtils.get(ProductStatRepository.class).findOne(filter).orElse(null);
				}
				if (prodStat == null) {
					prodStat = data;
				} else {
					prodStat.setPartCount(data.getPartCount());
					prodStat.setSupplierCount(data.getSupplierCount());
					prodStat.setMoldCount(data.getMoldCount());
					prodStat.setProduced(data.getProduced());
					prodStat.setProducedVal(data.getProducedVal());
					prodStat.setDailyCapacity(data.getDailyCapacity());
				}

				CacheDataUtils.save("ProductStat.inv", product.getId().toString(), _invs);
				BeanUtils.get(ProductStatRepository.class).save(prodStat);
			});

		} while (today.compareTo((day[0] = ProductUtils.getNextDay(day[0]))) >= 0);
	}

	private static boolean consume(ProductStat data, List<Part> parts, Map<Long, Map<String, Long>> invs) {
		Boolean consumable = null;
		for (Part part : parts) {
			long rq = ValueUtils.toLong(part.getQuantityRequired(), 0L);
			if (rq > 0) {
				if (consumable == null) {
					consumable = true;
				}
				consumable = consumable && invs.containsKey(part.getId()) && invs.get(part.getId()).get("producedVal") >= rq;
				if (!consumable) {
					break;
				}
			}
		}
		if (consumable == null || !consumable) {
			return false;
		}

		data.setProduced(data.getProduced() + 1);
		data.setProducedVal(data.getProducedVal() + 1);
		for (Part part : parts) {
			long rq = ValueUtils.toLong(part.getQuantityRequired(), 0L);
			if (rq > 0) {
				Map<String, Long> produceds = invs.get(part.getId());
				long produced = Math.max(0L, produceds.get("produced") - rq);
				long producedVal = Math.max(0L, produceds.get("producedVal") - rq);
				if (producedVal == 0L) {
					invs.remove(part.getId());
					consumable = false;
				} else {
					produceds.put("produced", produced);
					produceds.put("producedVal", producedVal);
				}
			}
		}
		return consumable;
	}

//	private static boolean isEmpty(Integer value) {
//		return value == null || value.equals(0);
//	}

	@Getter
	@Setter
	@NoArgsConstructor
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class PartSummary extends CapaSummary {
		public PartSummary(long produced, long producedVal) {
			this.produced = produced;
			this.producedVal = producedVal;
		}

		private long produced;
		private long producedVal;
	}

	private PartSummary getPartSummary(Category product, Part part, String week, String year, String month, String day) {
		LogicUtils.assertNotNull(part, "part");
		LogicUtils.assertNotEmpty(week, "week");
		LogicUtils.assertNotEmpty(year, "year");
		LogicUtils.assertNotEmpty(month, "month");
		LogicUtils.assertNotEmpty(day, "day");
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.partStat.day.eq(day))//
				.and(Q.partStat.partId.eq(part.getId()));
		QueryUtils.eq(filter, Q.partStat.productId, product == null ? null : product.getId());

		PartSummary total = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						PartSummary.class, //
						Q.partStat.produced.sum(), //
						Q.partStat.producedVal.sum()//
				))//
				.from(Q.partStat)//
				.where(filter)//
				.fetchOne();

		ProductUtils.setDailyCapaSummary(total, day, part.getId());
		return total;
	}

	private void summarizePartBatch() {
		DataUtils.runBatch(PartRepository.class, //
				QueryUtils.isPart(), Sort.by("id"), 10, true, //
				part -> summarizePartBatch(part)//
		);
	}

	private void summarizePartBatch(Part part) {
		String[] day = { getLastPartStatDay(null, part.getId()) };
		String today = DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyyMMdd, Zone.SYS);
		do {
			String week = DateUtils2.toOtherPattern(day[0], DatePattern.yyyyMMdd, DatePattern.YYYYww);
			String year = day[0].substring(0, 4);
			String month = day[0].substring(0, 6);
			summarizePart(part, week, year, month, day[0]);
		} while (today.compareTo((day[0] = ProductUtils.getNextDay(day[0]))) >= 0);
	}

	private PartSummary summarizePart(Part part, String week, String year, String month, String day) {
		return TranUtils.doNewTran(() -> {
			PartSummary total = new PartSummary();

			BeanUtils.get(PartStatRepository.class).deleteAll(//
					BeanUtils.get(PartStatRepository.class).findAll(//
							new BooleanBuilder()//
									.and(Q.partStat.partId.eq(part.getId()))//
									.and(Q.partStat.day.eq(day))//
			));
			BeanUtils.get(PartStatRepository.class).flush();

			ProductUtils.setDailyCapaSummary(total, day, part.getId());

			if (ObjectUtils.isEmpty(total.getMolds())) {
				return total;
			}

			Set<Long> moldIds = new HashSet<>();
			for (Mold mold : total.getMolds().values()) {
				if (moldIds.contains(mold.getId())) {
					continue;
				}
				moldIds.add(mold.getId());

				PartStat partStat = new PartStat();
				partStat.setPartId(part.getId());
				partStat.setSupplierId(mold.getLocation() == null ? mold.getCompanyId() : mold.getLocation().getCompanyId());
				partStat.setLocationId(mold.getLocationId());
				partStat.setMoldId(mold.getId());
				partStat.setWeek(week);
				partStat.setYear(year);
				partStat.setMonth(month);
				partStat.setDay(day);
				partStat.setDailyCapacity(ProductUtils.getDailyCapa(mold));

				boolean[] saved = { false };

				BeanUtils.get(JPAQueryFactory.class)//
						.select(Projections.constructor(//
								PartStatItem.class, //
								Q.part.categoryId, //
								Q.statisticsExt.shotCountCtt.multiply(Q.statisticsPart.cavity).sum()//
										.coalesce(0)//
										.as("produced"), //
								Q.statisticsExt.shotCountCtt.multiply(Q.statisticsPart.cavity).sum()//
										.coalesce(0)//
										.as("producedVal")))//
						.from(Q.statisticsPart)//
						.innerJoin(Q.part).on(Q.part.id.eq(Q.statisticsPart.partId))//
						.innerJoin(Q.statisticsExt).on(Q.statisticsExt.id.eq(Q.statisticsPart.statisticsId))//
						.where(new BooleanBuilder()//
								.and(Q.statisticsPart.statistics.day.eq(day))//
								.and(Q.statisticsPart.partId.eq(part.getId()))//
								.and(Q.statisticsPart.statistics.moldId.eq(mold.getId())))//
						.groupBy(Q.part.categoryId)//
						.fetch().forEach(partStatItem -> {
							if (saved[0]) {
								LogUtils.saveErrorQuietly(ErrorType.LOGIC, "PART_STAT_DUPLICATED", HttpStatus.NOT_IMPLEMENTED, "How is it can be duplicated");
								return;
							}
							saved[0] = true;

							partStat.setProductId(partStatItem.getProductId());

							partStat.setProduced(partStatItem.getProduced());
							total.setProduced(total.getProduced() + partStatItem.getProduced());
							partStat.setProducedVal(partStatItem.getProducedVal());
							total.setProducedVal(total.getProducedVal() + partStatItem.getProducedVal());
							try {
								BeanUtils.get(PartStatRepository.class).save(partStat);
							} catch (Exception e) {
								AbstractException ae = LogUtils.saveErrorQuietly(ErrorType.LOGIC, //
										"PART_STAT_DUPLICATED", //
										HttpStatus.NOT_IMPLEMENTED, //
										"How is it can be duplicated", //
										e//
								);
								throw ae;
							}
						});

				if (partStat.getId() == null) {
					BeanUtils.get(PartStatRepository.class).findAll(new BooleanBuilder()//
							.and(Q.partStat.day.loe(day))//
							.and(Q.partStat.partId.eq(part.getId()))//
							.and(Q.partStat.productId.isNotNull()), //
							PageRequest.of(0, 1, Direction.DESC, "day", "id"))//
							.forEach(item -> partStat.setProductId(item.getProductId()));
					BeanUtils.get(PartStatRepository.class).save(partStat);
				}
			}
			return total;
		});
	}

	private void cleanPartBatch(Part part) {
		deletePartBatch(part);
		summarizePartBatch(part);
	}

	private void deletePartBatch(Part part) {
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.partStat.partId, part == null ? null : part.getId());
		DataUtils.runContentBatch(//
				PartStatRepository.class, //
				filter, //
				Sort.by("id"), 1000, false, //
				content -> TranUtils.doNewTran(() -> BeanUtils.get(PartStatRepository.class).deleteAll(content))//
		);
	}

	private void cleanProductBatch(Category product) {
		deleteProductBatch(product);
		summarizeProductBatch(product);
	}

	private void deleteProductBatch(Category product) {
		{
			BooleanBuilder filter = new BooleanBuilder();
			QueryUtils.eq(filter, Q.productStat.productId, product == null ? null : product.getId());

			DataUtils.runContentBatch(//
					ProductStatRepository.class, //
					filter, //
					Sort.by("id"), 1000, false, //
					content -> TranUtils.doNewTran(() -> BeanUtils.get(ProductStatRepository.class).deleteAll(content))//
			);
		}

		{
			BooleanBuilder filter = new BooleanBuilder()//
					.and(Qs.cacheData.cacheName.eq("ProductStat.inv"));
			QueryUtils.eq(filter, Qs.cacheData.cacheKey, product == null ? null : product.getId().toString());
			DataUtils.runContentBatch(//
					CacheDataRepository.class, //
					filter, //
					Sort.by("id"), 1000, false, //
					content -> TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class).deleteAll(content))//
			);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PartStatItem {
		private Long productId;
//		private Long partId;
//		private Long moldId;
//		private String week;
//		private String year;
//		private String month;
//		private String day;
		private Integer produced;
		private Integer producedVal;
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void cleanBatch(ProductStatCleanBatchIn input) {
		JobUtils.runIfNotRunning("sc.prod.summarizeBatch", new JobOptions().setClustered(true), () -> {

			if (!ObjectUtils.isEmpty(input.getPartId())) {
				input.getPartId().forEach(partId -> {
					Part part = BeanUtils.get(PartRepository.class).findById(partId).orElse(null);
					cleanPartBatch(part);
				});

			} else if (input.isEachPartStats()) {
				DataUtils.runBatch(PartRepository.class, //
						QueryUtils.isPart(), Sort.by("id"), 10, true, //
						part -> cleanPartBatch(part)//
				);

			} else if (input.isAllPartStats()) {
				deletePartBatch(null);
			}

			if (!ObjectUtils.isEmpty(input.getProductId())) {
				input.getProductId().forEach(productId -> {
					Category product = BeanUtils.get(CategoryRepository.class).findById(productId).orElse(null);
					cleanProductBatch(product);
				});

			} else if (input.isEachProductStats()) {
				DataUtils.runBatch(CategoryRepository.class, //
						QueryUtils.isProduct(Q.category), Sort.by("id"), 10, true, //
						product -> cleanProductBatch(product)//
				);

			} else if (input.isAllProductStats()) {
				deleteProductBatch(null);
			}

			if (input.isAllPartStats()) {
				summarizePartBatch();
			}
			if (input.isAllProductStats()) {
				summarizeProductBatch();
			}

//			String day = "20210625";
//
//			DataUtils.runBatch(PartRepository.class, QueryUtils.isPart(), Sort.by("id"), 100, true, part -> {
//				TranUtils.doNewTran(() -> {
//					if (BeanUtils.get(JPAQueryFactory.class)//
//							.select(Q.statistics.id)//
//							.from(Q.statistics)//
//							.innerJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
//							.where(Q.statistics.day.eq(day).and(Q.statisticsPart.partId.eq(part.getId())))//
//							.fetchOne() == 0) {
//						if (BeanUtils.get(JPAQueryFactory.class)//
//								.select(Q.statistics.id)//
//								.from(Q.statistics)//
//								.innerJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
//								.where(Q.statistics.day.lt(day).and(Q.statisticsPart.partId.eq(part.getId())))//
//								.fetchOne() == 0) {
//							return;
//						}
//						PartStat partStat = new PartStat();
//						partStat.setProductId(part.getCategoryId());
//						partStat.setPartId(part.getId());
////						partStat.setMoldId()
//						return;
//					}
//					BooleanBuilder filter = new BooleanBuilder().and(Q.statistics.day.eq(day)).and(Q.statisticsPart.partId.eq(part.getId()));
//					DataUtils.runBatch(BeanUtils.get(JPAQueryFactory.class)//
//							.select(Projections.constructor(//
//									PartStatItem.class, //
//									Q.part.categoryId, //
//									Q.statisticsPart.partId, //
//									Q.statistics.moldId, //
//									Q.statistics.week.max(), //
//									Q.statistics.year.max(), //
//									Q.statistics.month.max(), //
//									Q.statistics.day, //
//									Q.statisticsExt.shotCountCtt.multiply(Q.statisticsPart.cavity).sum()//
//											.coalesce(Q.statistics.shotCount.multiply(Q.statisticsPart.cavity).sum())//
//											.as("produced"), //
//									Q.statisticsExt.shotCountCtt.multiply(Q.statisticsPart.cavity).sum()//
//											.coalesce(Q.statistics.shotCount.multiply(Q.statisticsPart.cavity).sum())//
//											.as("producedVal")))//
//							.from(Q.statistics)//
//							.leftJoin(Q.statisticsExt).on(Q.statisticsExt.id.eq(Q.statistics.id))//
////							.innerJoin(Q.cdata).on(Q.cdata.id.eq(Q.statistics.cdataId))//
//							.innerJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
//							.innerJoin(Q.part).on(Q.part.id.eq(Q.statisticsPart.partId))//
//							.where(filter)//
//							.groupBy(//
//									Q.part.categoryId, //
//									Q.statisticsPart.partId, //
//									Q.statistics.moldId, //
//									Q.statistics.day)//
//							.orderBy(//
//									Q.part.categoryId.asc(), //
//									Q.statisticsPart.partId.asc(), //
//									Q.statistics.moldId.asc(), //
//									Q.statistics.day.asc())//
//							.orderBy(Q.statistics.id.asc()), //
//							1000, true, //
//							partStatItem -> {
//								TranUtils.doNewTran(() -> {
//									PartStat partStat = ValueUtils.map2(partStatItem, PartStat.class);
//
//									// Supplier / Plant / Daily Capacity
//									if (partStat.getMoldId() != null) {
//										Instant instant = DateUtils2.toInstant(partStat.getDay(), DatePattern.yyyyMMdd, Zone.GMT);
//										Long moldId = partStat.getMoldId();
//
//										BeanUtils.get(MoldLocationRepository.class).findAll(//
//												Q.moldLocation.moldId.eq(moldId).and(Q.moldLocation.notificationAt.loe(instant)), //
//												PageRequest.of(0, 1, Direction.DESC, "notificationAt")//
//										).forEach(moldLocation -> {
//											partStat.setLocationId(moldLocation.getLocationId());
//											partStat.setSupplierId(moldLocation.getLocation() == null ? null : moldLocation.getLocation().getCompanyId());
//										});
//										if (partStat.getLocationId() == null) {
//											BeanUtils.get(MoldLocationRepository.class).findAll(//
//													Q.moldLocation.moldId.eq(moldId), //
//													PageRequest.of(0, 1, Direction.ASC, "notificationAt")//
//											).forEach(moldLocation -> {
//												partStat.setLocationId(moldLocation.getLocationId());
//												partStat.setSupplierId(moldLocation.getLocation() == null ? null : moldLocation.getLocation().getCompanyId());
//											});
//										}
//										if (partStat.getLocationId() == null) {
//											Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
//											if (mold != null) {
//												partStat.setLocationId(mold.getLocationId());
//												partStat.setSupplierId(mold.getLocation() == null ? null : mold.getLocation().getCompanyId());
//											}
//										}
//										QMoldVersion qMoldVersion = QMoldVersion.moldVersion;
//										BeanUtils.get(MoldVersionRepository.class).findAll(//
//												qMoldVersion.originId.eq(moldId).and(qMoldVersion.createdAt.loe(instant)), //
//												PageRequest.of(0, 1, Direction.DESC, "createdAt")//
//										).forEach(mold -> {
//											int workDays = 7;
////										if (ValueUtils.isNumber(mold.getShiftsPerDay())) {
////											workDays = ValueUtils.toInteger(mold.getShiftsPerDay(), 0);
////										}
//											Integer dailyCapacity = workDays == 0 ? 0//
//													: mold.getMaxCapacityPerWeek() == null || mold.getMaxCapacityPerWeek().equals(0) ? 0 : //
//															mold.getMaxCapacityPerWeek() / workDays;
//											partStat.setDailyCapacity(dailyCapacity);
//										});
//										if (partStat.getDailyCapacity() == 0) {
//											BeanUtils.get(MoldVersionRepository.class).findAll(//
//													qMoldVersion.originId.eq(moldId).and(qMoldVersion.createdAt.gt(instant)), //
//													PageRequest.of(0, 1, Direction.ASC, "createdAt")//
//											).forEach(mold -> {
//												int workDays = 7;
////											if (ValueUtils.isNumber(mold.getShiftsPerDay())) {
////												workDays = ValueUtils.toInteger(mold.getShiftsPerDay(), 0);
////											}
//												Integer dailyCapacity = workDays == 0 ? 0//
//														: mold.getMaxCapacityPerWeek() == null || mold.getMaxCapacityPerWeek().equals(0) ? 0 : //
//																mold.getMaxCapacityPerWeek() / workDays;
//												partStat.setDailyCapacity(dailyCapacity);
//											});
//										}
//										if (partStat.getDailyCapacity() == 0) {
//											Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
//											if (mold != null) {
//												int workDays = 7;
////											if (ValueUtils.isNumber(mold.getShiftsPerDay())) {
////												workDays = ValueUtils.toInteger(mold.getShiftsPerDay(), 0);
////											}
//												Integer dailyCapacity = workDays == 0 ? 0//
//														: mold.getMaxCapacityPerWeek() == null || mold.getMaxCapacityPerWeek().equals(0) ? 0 : //
//																mold.getMaxCapacityPerWeek() / workDays;
//												partStat.setDailyCapacity(dailyCapacity);
//											}
//										}
//									}
//
////									BeanUtils.get(PartStatRepository.class).save(partStat);
//								});
//							});
//				});
//			});
		});
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustBatch() {

//		// Adjust Location
//		DataUtils.runBatch(//
//				BeanUtils.get(JPAQueryFactory.class)//
//						.selectFrom(Q.partStat)//
//						.innerJoin(Q.mold).on(Q.mold.id.eq(Q.partStat.moldId))//
//						.where(Q.partStat.locationId.isNull().and(Q.mold.locationId.isNotNull()))//
//						.orderBy(Q.partStat.id.asc()), //
//				1000, false, //
//				partStat -> {
//					TranUtils.doNewTran(() -> {
//						partStat.setLocationId(partStat.getMold().getLocationId());
//						BeanUtils.get(PartStatRepository.class).save(partStat);
//					});
//				});
//
//		// Adjust Week
//		DataUtils.runBatch(//
//				BeanUtils.get(JPAQueryFactory.class)//
//						.selectFrom(Q.partStat)//
//						.where(Q.partStat.year.eq("2023"))//
//						.orderBy(Q.partStat.id.asc()), //
//				1000, true, //
//				partStat -> {
//					Instant instant = DateUtils2.toInstant(partStat.getDay(), DatePattern.yyyyMMdd, Zone.GMT);
//					String week = DateUtils2.format(instant, DatePattern.YYYYww, Zone.GMT);
//					if (week.equals(partStat.getWeek())) {
//						return;
//					}
//					TranUtils.doNewTran(() -> {
//						partStat.setWeek(week);
//						BeanUtils.get(PartStatRepository.class).save(partStat);
//					});
//				});
	}

	private String getLastProdStatDay(Long productId) {
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.productStat.day.isNotNull())//
				.and(Q.productStat.day.isNotEmpty());
		QueryUtils.eq(filter, Q.productStat.productId, productId);
		ProductStat lastProdStat = findLastOne(ProductStatRepository.class, filter);
		if (lastProdStat != null) {
			String day = lastProdStat.getDay();
			DateUtils2.plus(day, DatePattern.yyyyMMdd, Duration.ofDays(-1L));
			return day;
		}

		String day = getFirstPartDay(productId, null);
		return day;
	}

	private String getLastPartStatDay(Long productId, Long partId) {
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.partStat.day.isNotNull())//
				.and(Q.partStat.day.isNotEmpty());
		QueryUtils.eq(filter, Q.partStat.productId, productId);
		QueryUtils.eq(filter, Q.partStat.partId, partId);
		PartStat lastPartStat = findLastOne(PartStatRepository.class, filter);
		// If there is at least one PartStat data of Current Product, we start Calculation from this Last Day
		if (lastPartStat != null) {
			String day = lastPartStat.getDay();
			DateUtils2.plus(day, DatePattern.yyyyMMdd, Duration.ofDays(-1L));
			return day;
		}

		String day = getFirstPartDay(productId, partId);
		return day;
	}

	private static <T> T findLastOne(//
			Class<? extends QuerydslPredicateExecutor<T>> repoClass, //
			Predicate filter//
	) {
		Page<T> page = TranUtils.doNewTran(() -> BeanUtils.get(repoClass).findAll(//
				filter, //
				PageRequest.of(0, 1, Sort.by(Direction.DESC, "day"))//
		));
		T lastOne = page.isEmpty() ? null : page.getContent().get(0);
		return lastOne;
	}

	private String getFirstPartDay(Long productId, Long partId) {
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.statistics.day.isNotNull())//
				.and(Q.statistics.day.isNotEmpty());
		QueryUtils.eq(filter, Q.statisticsPart.projectId, productId);
		QueryUtils.eq(filter, Q.statisticsPart.partId, partId);
		String day = TranUtils.doNewTran(() -> {
			return BeanUtils.get(JPAQueryFactory.class)//
					.select(Q.statistics.day)//
					.from(Q.statistics)//
					.leftJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
					.where(filter)//
					.orderBy(Q.statistics.createdAt.asc())//
					.fetchFirst();
		});
		if (day == null) {
			String yesterday = DateUtils2.format(DateUtils2.getInstant().minus(Duration.ofDays(1L)), DatePattern.yyyyMMdd, Zone.SYS);
			return yesterday;
		}
		String since = ProductUtils.getStatSince();
		if (day.compareTo(since) < 0) {
			day = since;
		}
		return day;
	}
}
