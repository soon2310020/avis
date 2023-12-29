package saleson.api.statistics;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.base.data.repository.statisticslite.QStatisticsLite;
import com.emoldino.api.analysis.resource.base.data.repository.statisticslite.StatisticsLite;
import com.emoldino.api.analysis.resource.base.data.repository.statisticslite.StatisticsLiteRepository;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.category.CategoryRepository;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.util.DateUtils;
import saleson.dto.TransactionDTO;
import saleson.model.Statistics;
import saleson.model.StatisticsPart;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldStatisticsDate;
import saleson.model.data.StatisticsFullData;

@Slf4j
@Service
public class StatisticsService {
	@Autowired
	private StatisticsRepository statisticsRepository;
	@Autowired
	private StatisticsPartRepository statisticsPartRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	public List<StatisticsFullData> convertStatisticsToStatisticsFullData(Page<Statistics> statistics) {
		List<StatisticsFullData> statisticsFullDataPage = new ArrayList<>();

		if (statistics == null || statistics.getTotalElements() == 0) {
			return statisticsFullDataPage;
		}

		List<Long> idList = new ArrayList<>();
		statistics.forEach(data -> {
			idList.add(data.getId());
		});
		List<StatisticsPart> statisticsPartList = statisticsPartRepository.findAllByStatisticsIdIn(idList);
		statistics.forEach(data -> {
			StatisticsFullData result = new StatisticsFullData(data);
			int cavity = statisticsPartList.stream().filter(x -> x.getStatisticsId().equals(data.getId())).map(m -> m.getCavity()).findAny().orElse(0);
			result.setCavity(cavity);
			statisticsFullDataPage.add(result);
		});
		return statisticsFullDataPage;
	}

	public List<MiniComponentData> findAllMiniData() {
		List<MiniComponentData> projects = statisticsPartRepository.findAllWithProjectIdProjectName();
		projects.stream().filter(p -> p.getId() != null).forEach(p -> categoryRepository.findById(p.getId()).ifPresent(category -> p.setName(category.getName())));
		return projects.stream().filter(m -> m.getName() != null).collect(Collectors.toList());
	}

	public List<MoldStatisticsDate> getDataTimeRange(Long moldId, Long partId) {
		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);

		List<MoldStatisticsDate> result = new ArrayList<>();
		if (moldId != null) {
			StatisticsLite first = null;
			{
				StatisticsLite[] data = { null };
				QStatisticsLite table = QStatisticsLite.statisticsLite;
				BooleanBuilder filter = new BooleanBuilder()//
						.and(table.moldId.eq(moldId))//
						.and(table.day.isNotEmpty())//
						.and(dataFilterEnabled ? table.ctVal.gt(0D) : table.ct.gt(0D));
				BeanUtils.get(StatisticsLiteRepository.class).findAll(filter, PageRequest.of(0, 3000, Direction.ASC, "id"))//
						.forEach(stat -> data[0] = data[0] == null || data[0].getDay().compareTo(stat.getDay()) > 0 ? stat : data[0]);
				first = data[0];
			}

//			Statistics first = null;
//			Optional<List<Statistics>> firstOptional = dataFilterEnabled
//					? statisticsRepository.findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByDayAsc(moldId, PageRequest.of(0, 1))
//					: statisticsRepository.findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByDayAsc(moldId, PageRequest.of(0, 1));
//			if (firstOptional.isPresent()) {
//				first = firstOptional.get().get(0);
//			}
			if (first == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
				moldStatisticsDate.setMoldId(moldId);
				result.add(moldStatisticsDate);
			} else {
				MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(first.getDay());
				moldStatisticsDate.setMoldId(moldId);
				result.add(moldStatisticsDate);
			}

			StatisticsLite last = null;
			{
				StatisticsLite[] data = { null };
				QStatisticsLite table = QStatisticsLite.statisticsLite;
				BooleanBuilder filter = new BooleanBuilder()//
						.and(table.moldId.eq(moldId))//
						.and(table.day.isNotEmpty())//
						.and(dataFilterEnabled ? table.ctVal.gt(0D) : table.ct.gt(0D));
				BeanUtils.get(StatisticsLiteRepository.class).findAll(filter, PageRequest.of(0, 3000, Direction.DESC, "id"))//
						.forEach(stat -> data[0] = data[0] == null || data[0].getDay().compareTo(stat.getDay()) < 0 ? stat : data[0]);
				last = data[0];
			}

//			Optional<List<Statistics>> lastOptional = dataFilterEnabled
//					? statisticsRepository.findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByDayDesc(moldId, PageRequest.of(0, 1))
//					: statisticsRepository.findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByDayDesc(moldId, PageRequest.of(0, 1));
//			if (lastOptional.isPresent()) {
//				last = lastOptional.get().get(0);
//			}
			if (last == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
				moldStatisticsDate.setMoldId(moldId);
				result.add(moldStatisticsDate);
			} else {
				MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(last.getDay());
				moldStatisticsDate.setMoldId(moldId);
				result.add(moldStatisticsDate);
			}

			return result;
		} else if (partId != null) {
			StatisticsPart first = null;
			try {
				Optional<List<StatisticsPart>> firstOptional = statisticsPartRepository.findFirstByPartIdAndStatisticsExistsOrderByCreatedAtAsc(partId, PageRequest.of(0, 1));
				if (firstOptional.isPresent()) {
					first = firstOptional.get().get(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (first == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
				moldStatisticsDate.setPartId(partId);
				result.add(moldStatisticsDate);
			} else {
				String firstShotTime = first.getStatistics().getFst() != null ? first.getStatistics().getFst()
						: (first.getStatistics().getLst() != null ? first.getStatistics().getLst() : null);
				if (firstShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(firstShotTime);
					moldStatisticsDate.setPartId(partId);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
					moldStatisticsDate.setPartId(partId);
					result.add(moldStatisticsDate);
				}
			}

			StatisticsPart last = null;
			Optional<List<StatisticsPart>> lastOptional = statisticsPartRepository.findFirstByPartIdAndStatisticsExistsOrderByCreatedAtDesc(partId, PageRequest.of(0, 1));
			if (lastOptional.isPresent()) {
				last = lastOptional.get().get(0);
			}
			if (last == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
				moldStatisticsDate.setPartId(partId);
				result.add(moldStatisticsDate);
			} else {
				String lastShotTime = last.getStatistics().getLst() != null ? last.getStatistics().getLst()
						: (last.getStatistics().getFst() != null ? last.getStatistics().getFst() : null);
				if (lastShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(lastShotTime);
					moldStatisticsDate.setPartId(partId);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
					moldStatisticsDate.setPartId(partId);
					result.add(moldStatisticsDate);
				}
			}

			return result;
		} else {
			return result;
		}
	}

	public List<MoldStatisticsDate> getDataTimeRange(List<Long> moldIdList) {
		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);

		List<MoldStatisticsDate> result = new ArrayList<>();
		if (moldIdList != null && !moldIdList.isEmpty()) {

			Statistics first = null;
			Optional<List<Statistics>> firstOptional = dataFilterEnabled
					? statisticsRepository.findFirstByMoldIdListAndCtValGreaterThanOrFirstDataIsTrueOrderByDayAsc(moldIdList, PageRequest.of(0, 1))
					: statisticsRepository.findFirstByMoldIdListAndCtGreaterThanOrFirstDataIsTrueOrderByDayAsc(moldIdList, PageRequest.of(0, 1));
			if (firstOptional.isPresent()) {
				first = firstOptional.get().get(0);
			}
			if (first == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//				moldStatisticsDate.setMoldId(moldIdList);
				result.add(moldStatisticsDate);
			} else {
				String firstShotTime = first.getFst() != null ? first.getFst() : (first.getLst() != null ? first.getLst() : null);
				if (firstShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(firstShotTime);
//					moldStatisticsDate.setMoldId(moldIdList);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//					moldStatisticsDate.setMoldId(moldIdList);
					result.add(moldStatisticsDate);
				}
			}

			Statistics last = null;
			Optional<List<Statistics>> lastOptional = dataFilterEnabled
					? statisticsRepository.findFirstByMoldIdListAndCtValGreaterThanOrFirstDataIsTrueOrderByDayDesc(moldIdList, PageRequest.of(0, 1))
					: statisticsRepository.findFirstByMoldIdListAndCtGreaterThanOrFirstDataIsTrueOrderByDayDesc(moldIdList, PageRequest.of(0, 1));
			if (lastOptional.isPresent()) {
				last = lastOptional.get().get(0);
			}
			if (last == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//				moldStatisticsDate.setMoldId(moldIdList);
				result.add(moldStatisticsDate);
			} else {
				String lastShotTime = last.getLst() != null ? last.getLst() : (last.getFst() != null ? last.getFst() : null);
				if (lastShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(lastShotTime);
//					moldStatisticsDate.setMoldId(moldIdList);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//					moldStatisticsDate.setMoldId(moldIdList);
					result.add(moldStatisticsDate);
				}
			}
		}
		return result;

	}

	public static MoldStatisticsDate getCurrentDefaultDate() {
		MoldStatisticsDate moldStatisticsDate = MoldStatisticsDate.builder().build();
		Date date = new Date();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		moldStatisticsDate.setYear(localDate.getYear());
		moldStatisticsDate.setMonth(localDate.getMonthValue());
		moldStatisticsDate.setDay(localDate.getDayOfMonth());
		return moldStatisticsDate;
	}

	public static MoldStatisticsDate getMoldStaticsDateFromShotTime(String shotTime) {
		MoldStatisticsDate moldStatisticsDate = MoldStatisticsDate.builder().build();
		if (shotTime.length() >= 4) {
			moldStatisticsDate.setYear(Integer.valueOf(shotTime.substring(0, 4)));
		}
		if (shotTime.length() >= 6) {
			moldStatisticsDate.setMonth(Integer.valueOf(shotTime.substring(4, 6)));
		}
		if (shotTime.length() >= 8) {
			moldStatisticsDate.setDay(Integer.valueOf(shotTime.substring(6, 8)));
		}
		return moldStatisticsDate;
	}

	public String recoverHourData() {
		List<Statistics> statistics = statisticsRepository.findByHourIsNull();
		statistics.forEach(st -> {
			String time = st.getFst() != null ? st.getFst() : st.getLst();
			if (time.length() >= 10)
				st.setHour(time.substring(0, 10));
		});
		statisticsRepository.saveAll(statistics);
		return "hihi";
	}

	public boolean checkOldCounterData(Long moldId, String dateDetails) {
		if (dateDetails == null) {
			return false;
		}
		if (dateDetails.startsWith("W")) {
			if (!statisticsRepository.existsByMoldIdAndWeek(moldId, dateDetails.substring(1))) {
				return true;
			}

			return statisticsRepository.existsByMoldIdAndWeekAndCiLike(moldId, dateDetails.substring(1), "CMS%");
		} else if (dateDetails.startsWith("M")) {
			if (!statisticsRepository.existsByMoldIdAndMonth(moldId, dateDetails.substring(1))) {
				return true;
			}

			return statisticsRepository.existsByMoldIdAndMonthAndCiLike(moldId, dateDetails.substring(1), "CMS%");
		}
		if (!statisticsRepository.existsByMoldIdAndDay(moldId, dateDetails)) {
			return true;
		}

		return statisticsRepository.existsByMoldIdAndDayAndCiLike(moldId, dateDetails, "CMS%");
	}

	public List<TransactionDTO> aggregateTransactionByDay(String day) {
		return statisticsRepository.getTransactionByDay(day);
	}

	public Statistics firstStatistics(Long moldId) {
		Statistics first = statisticsRepository.findFirstByMoldIdAndScGreaterThanOrderByScAscIdAsc(moldId, 0).orElse(null);
		if (first == null) {
			first = statisticsRepository.findFirstByMoldIdOrderByLstAsc(moldId).orElse(null);
		}
		return first;
	}

	public List<Statistics> recoverFirstData() {
		List<Statistics> firstStatistics = statisticsRepository.findFirstStatistics();
		firstStatistics.forEach(x -> x.setFirstData(true));
		return statisticsRepository.saveAll(firstStatistics);
	}

	@Transactional
	public List<Statistics> recoverStatisticsWeek(String year) {
		List<Statistics> statistics = statisticsRepository.findByYear(year);
		statistics.forEach(data -> {
			data.setWeek(DateUtils.getYearWeek(data.getRt() != null ? data.getRt() : data.getLst()));
		});
		return statisticsRepository.saveAll(statistics);
	}
}
