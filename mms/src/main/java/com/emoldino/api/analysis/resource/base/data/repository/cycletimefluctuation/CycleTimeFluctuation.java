package com.emoldino.api.analysis.resource.base.data.repository.cycletimefluctuation;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CycleTimeFluctuation {
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
	private Double ctFluctuation;
	@Column(name = "L1_LIMIT")
	private Double l1Limit;
	private Double nctf;
	private Double ctfTrend;

	private Integer shotCount;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

}
