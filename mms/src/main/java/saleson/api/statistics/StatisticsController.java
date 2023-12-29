package saleson.api.statistics;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.statistics.payload.StatisticsPayload;
import saleson.model.Cdata;
import saleson.model.QCdata;
import saleson.model.Statistics;
import saleson.model.data.StatisticsFullData;
import saleson.service.transfer.CdataRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
	private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

	@Autowired
	CdataRepository cdataRepository;

	@Autowired
	StatisticsRepository statisticsRepository;

	@Autowired
	StatisticsService statisticsService;

	@GetMapping
	public ResponseEntity<Page<StatisticsFullData>> getAllCompanies(StatisticsPayload payload, Pageable pageable, Model model) {
		Page<Statistics> pageContent = statisticsRepository.findAll(payload.getPredicate(), pageable);
		List<StatisticsFullData> statisticsFullDataList = statisticsService.convertStatisticsToStatisticsFullData(pageContent);
		Page<StatisticsFullData> statisticsFullDataPage = new PageImpl<>(statisticsFullDataList, pageable, pageContent.getTotalElements());
		model.addAttribute("pageContent", statisticsFullDataPage);
//		return new ResponseEntity<>(pageContent, HttpStatus.OK);
		return new ResponseEntity<>(statisticsFullDataPage, HttpStatus.OK);
	}

	@GetMapping("/check-old-counter")
	public ResponseEntity<?> isOldCounterData(@RequestParam Long moldId,
											  @RequestParam String dateDetails){
		return new ResponseEntity<>(statisticsService.checkOldCounterData(moldId, dateDetails), HttpStatus.OK);
	}

	@GetMapping("time-range")
	public ResponseEntity<?> getDataTimeRange(@RequestParam(required = false) Long moldId, @RequestParam(required = false) Long partId){
		return new ResponseEntity<>(statisticsService.getDataTimeRange(moldId, partId), HttpStatus.OK);
	}
	@GetMapping("time-range-list")
	public ResponseEntity<?> getDataTimeRangeList(@RequestParam(required = false) List<Long> moldIdList){
		return new ResponseEntity<>(statisticsService.getDataTimeRange(moldIdList), HttpStatus.OK);
	}

	@PostMapping("recover-hour-data")
	public ResponseEntity<?> recoverHourData(){
		return new ResponseEntity<>(statisticsService.recoverHourData(), HttpStatus.OK);
	}

	@PostMapping("recover-first-data")
	public ResponseEntity<?> recoverFirstData(){
		return new ResponseEntity<>(statisticsService.recoverFirstData(), HttpStatus.OK);
	}

	@GetMapping("/recover-statistics-week")
	public ResponseEntity<?> recoverStatisticsWeek(@RequestParam String year){
		return new ResponseEntity<>(statisticsService.recoverStatisticsWeek(year), HttpStatus.OK);
	}

	//@RequestMapping
	public void test() {
		QCdata qcdata = QCdata.cdata;


		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(
				qcdata.ci.eq("CMS1744I04002")
		);

		Pageable pageable = PageRequest.of(0, 1000, new Sort(Sort.Direction.ASC, "lst"));


		Page<Cdata> cdataPage = cdataRepository.findAll(predicate, pageable);

		Cdata previousCdata = null;
		for (Cdata cdata : cdataPage.getContent()) {
			Statistics statistics = new Statistics(cdata);


			// shot count
			int shotCount = cdata.getSc();
			if (previousCdata == null) {
				statistics.setFsc(0);
				statistics.setShotCount(statistics.getSc());
			} else {
				statistics.setFsc(previousCdata.getSc());
				statistics.setShotCount(cdata.getSc() - previousCdata.getSc());
			}

			// 사이클 타임과 realShotCount로 시작 시간 계산
			long uptimeSeconds = (long) (statistics.getShotCount() * (statistics.getCt() * 0.1));

			// 작업시간이 0인경우 pass
			previousCdata = cdata;
			if (uptimeSeconds == 0) {
				continue;
			}


			statistics.setUptimeSeconds(uptimeSeconds);


			// 마지막 Shot 시간
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
								.withLocale(Locale.getDefault())
								.withZone(ZoneId.systemDefault());

			LocalDateTime lastShotTime = LocalDateTime.parse(statistics.getLst(), formatter);
			LocalDateTime firstShortTime = lastShotTime.minus(Duration.ofSeconds(uptimeSeconds));

			// 처음 shot 시간
			String fst = formatter.format(firstShortTime);

			statistics.setFst(fst);


			// 처음 시작 시간이 어제인 경우 데이터 나누기
			String firstDate = statistics.getFst().substring(0, 8);
			String lastDate = statistics.getLst().substring(0, 8);

			if (lastDate.equals(firstDate)) {
				statisticsRepository.save(statistics);

			} else {
				// 기준일
				String standardDate = lastDate + "000000";
				LocalDateTime standardDateTime = LocalDateTime.parse(standardDate, formatter);

				// 기준일 부터 lastShotTime 까지 시간차이 (sec)
				Long uptimeSeconds2 = Duration.between(standardDateTime, lastShotTime).toMinutes() * 60;

				// firstDate의 uptimeSecond 구하기
				Long uptimeSeconds1 = statistics.getUptimeSeconds() - uptimeSeconds2;

				// shotCount 구하기 : uptimeSeconds1 / uptimeSeconds2 비율로 각각의 shotCount 구하기
				int shotCount2 = (int) (statistics.getShotCount() * ((double) uptimeSeconds2 / (double) statistics.getUptimeSeconds()));
				int shotCount1 = statistics.getShotCount() - shotCount2;

				// 2개의 통계 데이터 등록
				try {
					Statistics s1 = (Statistics) statistics.clone();
					s1.setShotCount(shotCount1);
					s1.setUptimeSeconds(uptimeSeconds1);
					s1.setLst(standardDate);


					Statistics s2 = (Statistics) statistics.clone();
					s2.setShotCount(shotCount2);
					s2.setUptimeSeconds(uptimeSeconds2);
					s2.setFst(standardDate);

					statisticsRepository.save(s1);
					statisticsRepository.save(s2);

				} catch (Exception e) {
					logger.error("Statistics Clone Error : {}", e.getMessage());
				}
			}
		}
	}
}
