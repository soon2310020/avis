package saleson.service.transfer;


import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import saleson.common.util.DateUtils;
import saleson.model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.temporal.ChronoUnit.*;

@Slf4j
class TransferServiceTest {
	@Test
	void timeTest() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());
		DateTimeFormatter dayHourFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());


		Cdata cdata = new Cdata();
		cdata.setLst("20200415093320");
		cdata.setCt(10.0);
		cdata.setSc(10000);
		cdata.setId(3L);



		Statistics statistics = new Statistics(cdata);
		statistics.setFsc(100);
		statistics.setShotCount(9900);


		// 전체 작동 시간 (초) = 샷 증가량 * CT(s)
		long totalUptimeSeconds = (long) (statistics.getShotCount() * (statistics.getCt() * 0.1));

		LocalDateTime endDateInDay = LocalDateTime.parse(statistics.getLst(), formatter);
		LocalDateTime startDateInDay = endDateInDay.minus(Duration.ofSeconds(totalUptimeSeconds));

		long hours = HOURS.between(startDateInDay, endDateInDay) + 1;	// 표시 시간을 위해 + 1
		int baseFsc = statistics.getFsc();

		List<Statistics> insertStats = new ArrayList<>();
		for (long i = 0; i <= hours; i++) {
			LocalDateTime startDateTime = endDateInDay.minus(Duration.ofHours(hours - i));
			LocalDateTime endDateTime = endDateInDay.minus(Duration.ofHours(hours - (i + 1)));

			String baseStartTime = startDateTime.format(dayHourFormatter) + "0000";
			String baseEndTime = endDateTime.format(dayHourFormatter) + "0000";

			LocalDateTime startDate = LocalDateTime.parse(baseStartTime, formatter);
			LocalDateTime endDate = LocalDateTime.parse(baseEndTime, formatter);

			// 실제 시작 시간과 종료시간을 계산 (1시간 꽉 채우지 않는 경우)
			long startSeconds = SECONDS.between(startDate, startDateInDay);
			long endSeconds = SECONDS.between(endDate, endDateInDay);

			if (startSeconds > 0) startDate = startDateInDay;
			if (endSeconds < 0) endDate = endDateInDay;

			// 해당 시간 범위에 실제 동작 시간 계산
			long uptimeSeconds = SECONDS.between(startDate, endDate);

			if (uptimeSeconds <= 0) {
				continue;
			}

			int shotCount = (int) (((double) uptimeSeconds) / (statistics.getCt() / 10));
			int fsc = baseFsc;
			int sc = fsc + shotCount;
			String fst = startDate.format(formatter);
			String lst = endDate.format(formatter);

			Statistics stat = new Statistics();
			stat.setMoldId(statistics.getMoldId());
			stat.setMoldCode(statistics.getMoldCode());
			stat.setCdataId(cdata.getId());
			stat.setTi(statistics.getTi());
			stat.setCi(statistics.getCi());
			stat.setCt(statistics.getCt());

			stat.setFst(fst);
			stat.setLst(lst);
			stat.setFsc(fsc);
			stat.setSc(sc);
			stat.setShotCount(shotCount);
			stat.setUptimeSeconds(uptimeSeconds);

			stat.setHour(fst.substring(0, 10));
			stat.setYear(DateUtils.getYear(lst));
			stat.setMonth(DateUtils.getYearMonth(lst));
			stat.setWeek(DateUtils.getYearWeek(lst));
			stat.setDay(DateUtils.getDay(lst));
			stat.setCreatedAt(Instant.now());

			insertStats.add(stat);

			log.debug("## Stat. {}", stat);

			baseFsc = stat.getSc();

		}

//		for (long i = hours; i >= 0; i--) {
//			long hour = endDateInDay.minus(Duration.ofHours(i)).getHour();
//			log.debug("{}, {}", hour);
//		}


	}

	@Test
	void hourTest() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());

		long hours = HOURS.between(LocalDateTime.parse("20200415063020", formatter), LocalDateTime.parse("20200415093320", formatter));
		log.debug("{}", hours);
	}

	@Test
	void saveStatisticsTest() {

		Statistics previousStatistics = new Statistics();
		previousStatistics.setLst("20200413120000");
		previousStatistics.setCt(100d);
		previousStatistics.setSc(1);
		previousStatistics.setShotCount(1);


		Cdata cdata = new Cdata();
		cdata.setLst("20200414180000");
		cdata.setCt(100d);
		cdata.setSc(11200);


		List<Statistics> insertStatistics = getStatistics(previousStatistics, cdata);
		insertStatistics.stream().forEach(System.out::println);


	}

	private List<Statistics> getStatistics(Statistics previousStatistics, Cdata cdata) {
		List<Statistics> insertStats = new ArrayList<>();


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());

		Statistics statistics = new Statistics(cdata);


		// 이전 데이터 기준으로 fsc와 shotCount를 계산
		if (previousStatistics == null) {
			statistics.setFsc(0);
			statistics.setShotCount(statistics.getSc());
		} else {
			statistics.setFsc(previousStatistics.getSc());
			statistics.setShotCount(cdata.getSc() - previousStatistics.getSc());
		}


		// ShotCount == 0 이면 pass
		if (statistics.getShotCount() > 0) {

			//List<Statistics> insertStats = new ArrayList<>();
			LocalDateTime endDateTime = LocalDateTime.parse(statistics.getLst(), formatter);

			int baseShotCount = statistics.getShotCount();
			int lastSc = statistics.getSc();
			int total = 0;

			// 작동 시간 기준으로 시작날짜가 이전 일로 넘어가지 않을 때까지 처리.
			boolean hasNext = true;
			do {
				double ct = statistics.getCt();
				long uptimeSeconds = (long) (baseShotCount * (ct * 0.1));


				LocalDateTime startDate = endDateTime.minus(Duration.ofSeconds(uptimeSeconds));
				LocalDateTime endDate = endDateTime;

				int days = (int) DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
				int shotCount = baseShotCount;

				if (days == 0) {
					hasNext = false;


				} else {
					String date = endDateTime.toLocalDate().toString().replaceAll("-", "");
					startDate = LocalDateTime.parse(date + "000000", formatter);
					uptimeSeconds = SECONDS.between(startDate, endDate) + 1;
					shotCount = (int) (((double) uptimeSeconds) / (ct / 10));

					endDateTime = startDate.minus(Duration.ofSeconds(1));

				}

				String fst = formatter.format(startDate);
				String lst = formatter.format(endDate);

				int sc = lastSc;
				int fsc = lastSc - shotCount;

				baseShotCount = baseShotCount - shotCount;
				lastSc = fsc;


				Statistics stat = new Statistics();
				stat.setMoldId(statistics.getMoldId());
				stat.setMoldCode(statistics.getMoldCode());
				stat.setTi(statistics.getTi());
				stat.setCi(statistics.getCi());
				stat.setCt(statistics.getCt());

				stat.setFst(fst);
				stat.setLst(lst);
				stat.setFsc(fsc);
				stat.setSc(sc);
				stat.setShotCount(shotCount);
				stat.setUptimeSeconds(uptimeSeconds);

				stat.setYear(DateUtils.getYear(lst));
				stat.setMonth(DateUtils.getYearMonth(lst));
				stat.setWeek(DateUtils.getYearWeek(lst));
				stat.setDay(DateUtils.getDay(lst));
				stat.setCreatedAt(Instant.now());

				insertStats.add(stat);

			} while (hasNext);

		}
		return insertStats;
	}
}