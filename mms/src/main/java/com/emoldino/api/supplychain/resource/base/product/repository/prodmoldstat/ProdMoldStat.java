package com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_PROD_MOLD_STAT", columnNames = { "moldId", "productId", "partId", "supplierId", "week" }))
@DynamicUpdate
public class ProdMoldStat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long moldId;
	private Long productId;
	private Long partId;
	private Long supplierId;
	private String week;

	private Integer apprCycleTime;
	private Double weightedAvgCycleTime;
	private Long idealShotCount;
	private Long actualShotCount;
	private Long recentShotCount;
	private Double avgCycleTime;

	private Integer prodHoursPerDay;
	private Integer prodDays;
	private Integer targetUptimeRate;

	private Integer cavityCount;

	private Long weeklyCapa;

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
