package com.emoldino.api.analysis.resource.base.data.repository.statisticsext;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class StatisticsExt {
	@Id
	private Long id;
	private Long cdataId;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	private Integer shotCountCtt;
	private Integer shotCountCttVal;
}
