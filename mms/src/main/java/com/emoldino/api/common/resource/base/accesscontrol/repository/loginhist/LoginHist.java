package com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.enumeration.ClientType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = { //
		@Index(name = "IX_LOGIN_HIST_01", columnList = "userId,expiredAt"), //
		@Index(name = "IX_LOGIN_HIST_02", columnList = "clientId,loginAt"), //
		@Index(name = "IX_LOGIN_HIST_03", columnList = "messagingToken,loginAt") //
})
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class LoginHist {
	@Id
	private String sessionId;
	private Long userId;
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ClientType clientType;
	private String clientId;
	private Instant loginAt;
	private Instant logoutAt;
	private Instant expiredAt;
	private String messagingToken;

	@JsonIgnore
	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;
}
