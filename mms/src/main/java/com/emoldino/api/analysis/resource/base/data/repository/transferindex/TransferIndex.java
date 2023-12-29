package com.emoldino.api.analysis.resource.base.data.repository.transferindex;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "UX_TRANSFER_INDEX", columnList = "ci,tff", unique = true))
public class TransferIndex {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String ci;
	private String tff;

	@CreatedDate
	private Instant createdAt;

	public TransferIndex(String ci, String tff) {
		this.ci = ci;
		this.tff = tff;
	}
}
