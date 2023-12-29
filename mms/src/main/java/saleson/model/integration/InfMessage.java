package saleson.model.integration;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DynamicUpdate
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class InfMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String requestId;
	private String tenantId;
	private String type;

	private String correlationData;

	// I(INBOUND), O(OUTBOUND)
	private String infDirection;
	// CREATED, REQUESTED, REQ_ERROR, INVOKATION_ERROR, RETURNED, RET_ERROR, COMPLETED, ERROR
	private String procStatus;
	private Long procErrorId;

	// REST
	private String reqType;
	private String reqChannelName;
	private String reqTargetInfo;
	private String reqMessageClassName;
	private String reqMessage;

	private int invokationCount;
	private Instant lastInvokationTime;

	private int heartbeatCount;
	private Instant lastHeartbeatTime;

	// REST
	private String respType;
	private String respChannelName;
	private String respTargetInfo;
	private String respMessage;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
