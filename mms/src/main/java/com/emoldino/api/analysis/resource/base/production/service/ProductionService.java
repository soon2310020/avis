package com.emoldino.api.analysis.resource.base.production.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.production.dto.ProductionSummarizeBatchIn;
import com.emoldino.api.analysis.resource.base.production.repository.productstatistics.ProductStatistics;
import com.emoldino.api.analysis.resource.base.production.repository.productstatistics.ProductStatisticsRepository;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.category.CategoryService;
import saleson.api.category.payload.ProfileFilter;
import saleson.api.company.CompanyRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.model.Category;
import saleson.model.Part;
import saleson.model.QCategory;
import saleson.model.StatisticsPart;
import saleson.service.util.DateTimeUtils;

@Service
public class ProductionService {

	@Deprecated
	public void summarizeBatch(ProductionSummarizeBatchIn input) {
		JobUtils.runIfNotRunning("analysis.ProductionService.summarizeBatch", new JobOptions().setClustered(true), () -> {
			int i = 0;
			Page<Category> page;
			QCategory table = QCategory.category;
			BooleanBuilder builder = new BooleanBuilder();
			builder.and(table.level.eq(1));
			builder.and(table.enabled.eq(true));
			while (!(page = BeanUtils.get(CategoryService.class).findAll(builder, PageRequest.of(i++, 10, Direction.ASC, "id"))).isEmpty()) {
				page.forEach(item -> summarizeCategory(item, input));
			}
		});
	}

	@Deprecated
	private void summarizeCategory(Category category, ProductionSummarizeBatchIn input) {
		if (category.getLevel() == null || category.getLevel() != 1 || ObjectUtils.isEmpty(category.getChildren())) {
			return;
		}
		category.getChildren().forEach(item -> BeanUtils.get(ProductionService.class).summarizeProduct(item, input));
	}

	@Deprecated
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void summarizeProduct(Category product, ProductionSummarizeBatchIn input) {
		if (product.getLevel() == null || product.getLevel() != 2) {
			return;
		}

		ProductStatistics stat = BeanUtils.get(ProductStatisticsRepository.class).findById(product.getId()).orElse(null);
		if (stat == null) {
			stat = new ProductStatistics();
			stat.setId(product.getId());
			DataUtils.populate4Insert(stat);
		} else {
			DataUtils.populate4Update(stat);
		}

		stat.setPartCount(BeanUtils.get(PartRepository.class).countByProduct(product.getId(), null, null).intValue());
		stat.setMoldCount(BeanUtils.get(MoldRepository.class).countByProduct(product.getId(), null, null).intValue());
		stat.setSupplierCount(BeanUtils.get(CompanyRepository.class).countByProduct(product.getId(), null, null).intValue());

		Optional<StatisticsPart> firstProduced = BeanUtils.get(StatisticsPartRepository.class).findFirstByProjectIdOrderByCreatedAtAsc(product.getId());
		Long diffDate = firstProduced.isPresent() ? ValueUtils.toLong(DateTimeUtils.diffBetweenTwoDates(firstProduced.get().getCreatedAt(), DateUtils2.newInstant(), true), 0L)
				: 1L;

		// Total Produced, Weekly Max Capacity
		{
			ProductStatistics _stat = stat;
			stat.setTotalProduced(null);
			ProfileFilter filter = new ProfileFilter();
			filter.setProjectId(product.getId());

			Map<Long, Part> parts = new HashMap<>();
			Map<Long, Long> weeklyMaxCaps = new HashMap<>();

			BeanUtils.get(CategoryService.class).getPartPageByProduct(filter, PageRequest.of(0, 1000, Direction.ASC, "id")).forEach(part -> {
				if (part.isDeleted() || !part.isEnabled()) {
					return;
				}

				parts.put(part.getId(), part);

				// Total Produced
				if (part.getTotalProduced() == 0) {
					_stat.setTotalProduced(0L);
				} else {
					Long qtyRequired = Math.max(ValueUtils.toLong(part.getQuantityRequired(), 0L), 1L);
					long value = part.getTotalProduced() / qtyRequired;
					if (_stat.getTotalProduced() == null || _stat.getTotalProduced() > value) {
						_stat.setTotalProduced(value);
					}
				}

				// Weekly Max Capacity Per Part
				BeanUtils.get(MoldRepository.class).findByProject(_stat.getId(), part.getId(), null, PageRequest.of(0, 1000, Direction.ASC, "id")).forEach(mold -> {
					long value = weeklyMaxCaps.containsKey(part.getId()) ? weeklyMaxCaps.get(part.getId()) : 0L;
					weeklyMaxCaps.put(part.getId(), value + mold.getMaxCapacityPerWeek());
				});
			});

			if (stat.getTotalProduced() == null) {
				stat.setTotalProduced(0L);
			}

			// Weekly Max Capacity
			stat.setWeeklyMaxCapacity(null);

			parts.forEach((partId, part) -> {
				if (!weeklyMaxCaps.containsKey(partId)) {
					_stat.setWeeklyMaxCapacity(0L);
				} else {
					Long qtyRequired = Math.max(ValueUtils.toLong(part.getQuantityRequired(), 0L), 1L);
					long value = weeklyMaxCaps.get(partId) / qtyRequired;
					if (_stat.getWeeklyMaxCapacity() == null || _stat.getWeeklyMaxCapacity() > value) {
						_stat.setWeeklyMaxCapacity(value);
					}
				}
			});

			if (stat.getWeeklyMaxCapacity() == null) {
				stat.setWeeklyMaxCapacity(0L);
			}
		}

		// Total Max Capacity
		if (stat.getWeeklyMaxCapacity() == 0) {
			stat.setTotalMaxCapacity(0L);
		} else {
			stat.setTotalMaxCapacity(stat.getWeeklyMaxCapacity() / 7 * diffDate);
		}

		// Total Production Demand
		if (stat.getWeeklyMaxCapacity() == 0 || product.getWeeklyProductionDemand() == null || product.getWeeklyProductionDemand() == 0) {
			stat.setTotalProductionDemand(0L);
		} else {
			stat.setTotalProductionDemand((product.getWeeklyProductionDemand() / 7) * diffDate);
		}

		// Weekly Production Demand
		stat.setWeeklyProductionDemand(product.getWeeklyProductionDemand() == null ? 0L : product.getWeeklyProductionDemand());

		BeanUtils.get(ProductStatisticsRepository.class).save(stat);
	}

}
