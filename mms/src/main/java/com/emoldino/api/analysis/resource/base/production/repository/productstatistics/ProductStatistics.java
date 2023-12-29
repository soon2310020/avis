package com.emoldino.api.analysis.resource.base.production.repository.productstatistics;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Entity
//@DynamicUpdate
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class ProductStatistics {
	@Id
	private Long id;

	private Integer partCount;
	private Integer moldCount;
	private Integer supplierCount;

	private Long totalProduced = 0L;
	private Long totalMaxCapacity = 0L;
	private Long weeklyMaxCapacity = 0L;
	private Long totalProductionDemand = 0L;
	private Long weeklyProductionDemand = 0L;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
