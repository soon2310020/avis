package com.emoldino.api.supplychain.resource.base.product.repository.partstat;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;
import saleson.model.Company;
import saleson.model.Location;
import saleson.model.Mold;
import saleson.model.Part;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(name = "UX_PART_STAT", columnNames = { "productId", "partId", "supplierId", "locationId", "moldId", "day" }))
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PartStat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productId", insertable = false, updatable = false)
	private Category product;

	private Long partId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partId", insertable = false, updatable = false)
	private Part part;

	private Long supplierId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supplierId", insertable = false, updatable = false)
	private Company supplier;

	private Long locationId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locationId", insertable = false, updatable = false)
	private Location location;

	private Long moldId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moldId", insertable = false, updatable = false)
	private Mold mold;

	private String week;
	private String year;
	private String month;
	private String day;

	private long produced;
	private long producedVal;
//	private long hourlyCapacity;
	private long dailyCapacity;
//	private long weeklyCapacity;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

//	public PartStat(Long productId, Long partId, Long supplierId, Long moldId, String week, String year, String month, String day, long produced, long producedVal,
//			long dailyCapacity) {
//		this.productId = productId;
//		this.partId = partId;
//		this.supplierId = supplierId;
//		this.moldId = moldId;
//		this.week = week;
//		this.year = year;
//		this.month = month;
//		this.day = day;
//		this.produced = produced;
//		this.producedVal = producedVal;
//		this.dailyCapacity = dailyCapacity;
//	}

	public PartStat(Long productId, Long partId, Long supplierId, Long locationId, Long moldId, String week, String year, String month, String day, long produced, long producedVal,
			long dailyCapacity) {
		this.productId = productId;
		this.partId = partId;
		this.supplierId = supplierId;
		this.locationId = locationId;
		this.moldId = moldId;
		this.week = week;
		this.year = year;
		this.month = month;
		this.day = day;
		this.produced = produced;
		this.producedVal = producedVal;
		this.dailyCapacity = dailyCapacity;
	}
}
