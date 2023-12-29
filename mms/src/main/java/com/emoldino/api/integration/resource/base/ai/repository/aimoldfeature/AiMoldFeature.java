package com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AiMoldFeature {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long moldId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moldId", insertable = false, updatable = false)
	private Mold mold;

	private int scMin;
	@Column(name = "SC_5_PERC")
	private int scPerc5;
	@Column(name = "SC_10_PERC")
	private int scPerc10;
	@Column(name = "SC_15_PERC")
	private int scPerc15;
	@Column(name = "SC_20_PERC")
	private int scPerc20;
	@Column(name = "SC_25_PERC")
	private int scPerc25;
	@Column(name = "SC_50_PERC")
	private int scPerc50;
	@Column(name = "SC_75_PERC")
	private int scPerc75;

	private int scMax;
	private int scMean;
	private int scMode;
	private int uptimeMedian;
	private int uptimeMode;
	private int uptimeCount;
	private String moldClass;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
