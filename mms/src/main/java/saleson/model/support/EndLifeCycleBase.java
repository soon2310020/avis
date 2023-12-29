package saleson.model.support;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.util.DateUtils;
import saleson.model.Mold;
import saleson.model.User;

@Data
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class EndLifeCycleBase {
	@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID")
	private Mold mold;

	private Instant endLifeAt;
	private Instant calculationAt = Instant.now();
	private Instant issueDate;

	@Enumerated(EnumType.STRING)
	private PriorityType priority;

	@Enumerated(EnumType.STRING)
	private EndOfLifeCycleStatus status;
	private String year;

	private String description;

	@Column(name = "TOPIC_ID")
	private Long topicId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UPDATE_BY_ID")
	private User updateBy;

	private Integer forecastShotsPerDay;
	private Integer remainingDays;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	public String getEndLifeAtDate() {
		return DateUtils2.format(endLifeAt, DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByMold(getMold()));
	}

	public String getCalculationDate() {
		return DateUtils2.format(calculationAt, DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByMold(getMold()));
	}

	public EndLifeCycleBase(Mold mold, Instant issueDate, PriorityType priority, EndOfLifeCycleStatus status) {
		this.mold = mold;
		this.issueDate = issueDate;
		this.priority = priority;
		this.status = status;
	}

	public long getReachAfterYears() {
		if (endLifeAt != null) {
			return DateUtils.getCeilRoundedYearBetween(Instant.now(), endLifeAt);
		}
		return 0;
	}
}
