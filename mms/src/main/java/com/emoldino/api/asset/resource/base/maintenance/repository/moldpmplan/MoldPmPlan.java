package com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_MOLD_PM_PLAN", columnNames = "mold_Id"))
@DynamicUpdate
public class MoldPmPlan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mold_id")
	private Long moldId;

	@Enumerated(EnumType.STRING)
	private PM_STRATEGY pmStrategy;
	@Enumerated(EnumType.STRING)
	private PM_FREQUENCY pmFrequency;

	private String schedStartDate;
	private Integer schedInterval;
	private Integer schedOrdinalNum;
	private String schedDayOfWeek;
	private Integer schedUpcomingTolerance;

	@Enumerated(EnumType.STRING)
	private RECURR_CONSTRAINT_TYPE recurrConstraintType;
	private Integer recurrNum;
	private String recurrDueDate;

	private String nextSchedDate;
	private String nextUpcomingToleranceDate;

	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedBy
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;
}