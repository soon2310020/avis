package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.ObjectType;

@Getter
@Setter
@ToString(exclude = { "workOrder" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class WorkOrderAsset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "WORK_ORDER_ID", insertable = false, updatable = false)
	private Long workOrderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORK_ORDER_ID")
	private WorkOrder workOrder;

	private Long assetId;
	private String assetCode;

	@Enumerated(EnumType.STRING)
	private ObjectType type;

	@Transient
	private Long locationId;
	@Transient
	private String locationName;
	@Transient
	private String locationCode;

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;
	public WorkOrder getWorkOrder() {
		return workOrder;
	}
}
