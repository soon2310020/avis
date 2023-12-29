package com.emoldino.api.supplychain.resource.base.product.repository.productstat;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProductStat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productId", insertable = false, updatable = false)
	private Category product;

	private String week;
	private String year;
	private String month;
	private String day;

	private int partCount;
	private int supplierCount;
	private int moldCount;

	private long produced;
	private long producedVal;
//	private long hourlyCapacity;
	private long dailyCapacity;
//	private long weeklyCapacity;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	public ProductStat(Long productId, String week, String year, String month, String day, int partCount, int supplierCount, int moldCount, long produced, long producedVal,
			long dailyCapacity) {
		this.productId = productId;
		this.week = week;
		this.year = year;
		this.month = month;
		this.day = day;
		this.partCount = partCount;
		this.supplierCount = supplierCount;
		this.moldCount = moldCount;
		this.produced = produced;
		this.producedVal = producedVal;
//		this.hourlyCapacity = hourlyCapacity;
		this.dailyCapacity = dailyCapacity;
//		this.weeklyCapacity = weeklyCapacity;
	}
}
