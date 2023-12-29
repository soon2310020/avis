package com.emoldino.api.common.resource.base.version.repository.appversionitem;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Entity
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class AppVersionItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	private Long appVersionId;

	@JsonIgnore
	private Integer position;
	private String description;

	@JsonIgnore
	@CreatedBy
	private Long createdBy;
	@JsonIgnore
	@CreatedDate
	private Instant createdAt;

	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;
}
