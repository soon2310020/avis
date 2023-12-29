package com.emoldino.api.analysis.resource.base.data.repository.transfermessage;

import java.time.Instant;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class TransferMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String procStatus = "-";
	private Long procErrorId;
	private Long elapsedTimeMillis;

	private String at;
	private String ci;
	private String data;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	public TransferMessage(String at, String ci, String data) {
		this.at = at;
		this.ci = ci;
		this.data = data;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TransferData {
		private List<String> content;
	}
}
