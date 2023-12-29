package saleson.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import saleson.service.util.DateTimeUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static saleson.common.util.DateUtils.getLocalDate;

@Slf4j
class DateUtilsTest {

	@Test
	void getYesterday() {
		String yesterday = DateUtils.getYesterday("MMM");
		assertThat(yesterday).isEqualTo("Apr");

		log.debug("Alerts for {}", DateUtils.getYesterday("MMM dd, yyyy"));
		log.debug("Alerts For Week {}" , DateUtils.getWeekYear(DateUtils.getYesterday("yyyyMMddHHmmss"), "yyyyMMddHHmmss"));
	}

//	@Test
//	void getWeekOfYear() {
//		String weekYear = DateUtils.getWeekYear(DateUtils.getYesterday(), "yyyyMMddHHmmss");
//		String weekYear2 = DateUtils.getWeekYear(DateUtils.getYesterday());
//
//		assertThat(weekYear).isEqualTo(weekYear2);
//
//		log.debug("Alerts For Week1 {}", weekYear);
//		log.debug("Alerts For Week2 {}", weekYear2);
//	}

	@Test
	void fullMonth() {
		String yesterday = DateUtils.getYesterday("MMM dd, yyyy");
		log.debug("Alerts For {}", DateTimeUtils.getFullMonthName(yesterday.substring(0, 3)) + " " + yesterday.substring(8));
	}
}