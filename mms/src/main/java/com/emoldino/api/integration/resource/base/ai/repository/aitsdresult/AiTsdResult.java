package com.emoldino.api.integration.resource.base.ai.repository.aitsdresult;

import java.time.Instant;

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

import com.emoldino.api.integration.resource.composite.tsdwrk.dto.TsdLabel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Table(indexes = @Index(name = "UX_ATR", columnList = "STATISTICS_ID", unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AiTsdResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long moldId;
	private int lowScThreshold;
	private int highScThreshold;

	//@Column(name = "STATISTICS_ID", insertable = false, updatable = false)
	private String statisticsId;
	private String hour;
	private int shotCount;
	private int correctedShotCount;

	@Enumerated(EnumType.STRING)
	private TsdLabel tsdLabel;
	private int isMissing;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
