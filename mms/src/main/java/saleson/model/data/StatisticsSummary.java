package saleson.model.data;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PeriodType;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "UX_STATISTICS_SUMMARY", columnList = "periodType,year,month,day,week,moldId,ci", unique = true))
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class StatisticsSummary {
	public StatisticsSummary(String summaryGroup, PeriodType periodType, int year, int month, int day, int week, Long moldId, String ci) {
		this.summaryGroup = summaryGroup;
		this.periodType = periodType;
		this.year = year;
		this.month = month;
		this.day = day;
		this.week = week;
		this.moldId = moldId;
		this.ci = ci;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String summaryStatus = "SUMMARIZING";

	private String summaryGroup;

	@Enumerated(EnumType.STRING)
	private PeriodType periodType;
	private int year;
	private int month;
	private int day;
	private int week;
	private Long moldId;
	private String ci;
	private long uptimeSeconds;
	private double ct;
	private double ctVal;
	private long shotCount;
	private long shotCountVal;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
