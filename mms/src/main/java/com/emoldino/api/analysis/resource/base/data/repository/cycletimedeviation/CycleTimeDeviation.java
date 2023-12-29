package com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.enumeration.TimeScale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.data.cycleTime.ToolingCycleTimeData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CycleTimeDeviation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long supplierId;
	private Long moldId;
	private Long partId;

	@Enumerated(EnumType.STRING)
	private TimeScale timeScale;
	private String year;
	private String month;
	private String week;
	private String day;

	private Double approvedCycleTime;
	private Double averageCycleTime;
	private Double nctd;
	private Double nctdTrend;

	private Double aboveToleranceRate;
	@Column(name = "WITHIN_UPPER_L2_TOLERANCE_RATE")
	private Double withinUpperL2ToleranceRate;
	@Column(name = "WITHIN_L1_TOLERANCE_RATE")
	private Double withinL1ToleranceRate;
	@Column(name = "WITHIN_LOWER_L2_TOLERANCE_RATE")
	private Double withinLowerL2ToleranceRate;
	private Double belowToleranceRate;

	private Integer shotCount;
	private Integer aboveToleranceSc;
	@Column(name = "WITHIN_UPPER_L2_TOLERANCE_SC")
	private Integer withinUpperL2ToleranceSc;
	@Column(name = "WITHIN_L1_TOLERANCE_SC")
	private Integer withinL1ToleranceSc;
	@Column(name = "WITHIN_LOWER_L2_TOLERANCE_SC")
	private Integer withinLowerL2ToleranceSc;
	private Integer belowToleranceSc;

	//Cycle time deviation
	private Double ctFluctuation;
	@Column(name = "L1_LIMIT")
	private Double l1Limit;
	private Double nctf;
	private Double ctfTrend;


	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	public void bind(ToolingCycleTimeData data) {
		this.moldId = data.getMoldId();

		this.aboveToleranceSc = data.getShotCountAboveL2();
		this.withinUpperL2ToleranceSc = data.getShotCountAboveL1();
		this.withinL1ToleranceSc = data.getShotCountCompliance();
		this.withinLowerL2ToleranceSc = data.getShotCountBelowL1();
		this.belowToleranceSc = data.getShotCountBelowL2();
		this.shotCount = this.aboveToleranceSc + this.withinUpperL2ToleranceSc + this.withinL1ToleranceSc + this.withinLowerL2ToleranceSc + this.belowToleranceSc;

		this.aboveToleranceRate = data.getPercentageAboveL2();
		this.withinUpperL2ToleranceRate = data.getPercentageAboveL1();
		this.withinL1ToleranceRate = data.getPercentageCompliance();
		this.withinLowerL2ToleranceRate = data.getPercentageBelowL1();
		this.belowToleranceRate = data.getPercentageBelowL2();
	}
}
