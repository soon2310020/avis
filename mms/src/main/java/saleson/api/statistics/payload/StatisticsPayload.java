package saleson.api.statistics.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.DateViewType;
import saleson.common.util.StringUtils;
import saleson.model.QStatistics;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsPayload extends SearchParam {

	private Long moldId;
	private ChartDataType chartDataType;
	private DateViewType dateViewType;
	private String year;
	private String detailDate;

	public Predicate getPredicate() {
		QStatistics statistics = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();

		if (getMoldId() != null) {
			builder.and(statistics.moldId.eq(getMoldId()));
		}
		if (getYear() != null && !getYear().isEmpty()) {
			builder.and(statistics.year.eq(getYear()));
		}

		if (getDetailDate() != null && !getDetailDate().isEmpty()) {
			if (DateViewType.DAY == getDateViewType()) {
				String[] dates = StringUtils.delimitedListToStringArray(getDetailDate(), "/");

				if (dates.length == 2) {
					String day = getYear() + dates[0] + "" + dates[1];
					builder.and(statistics.day.eq(day));
				}

			} else if (DateViewType.WEEK == getDateViewType()) {
				String week = getYear() + "" + getDetailDate().replaceAll("W-", "");
				builder.and(statistics.week.eq(week));

			} else if (DateViewType.MONTH == getDateViewType()) {
				builder.and(statistics.month.eq(getYear() + "" + getDetailDate()));

			} else if (getDateViewType().equals(DateViewType.HOUR)) {
				builder.and(statistics.hour.eq(getDetailDate()));
			}

		}

		builder.and(statistics.shotCount.ne(0));

		return builder;
	}
}
