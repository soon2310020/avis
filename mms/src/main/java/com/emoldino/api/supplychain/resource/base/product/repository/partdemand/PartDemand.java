package com.emoldino.api.supplychain.resource.base.product.repository.partdemand;

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

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;
import saleson.model.Part;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PartDemand {
	public PartDemand(Long productId, Long partId, String periodType, String periodValue, Long quantity) {
		this.productId = productId;
		this.partId = partId;
		this.periodType = periodType;
		this.periodValue = periodValue;
		this.quantity = quantity;
	}

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

	private String periodType;
	private String periodValue;
	private Long quantity;

	@JsonIgnore
	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@LastModifiedDate
	private Instant updatedAt;
}
