package saleson.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.emoldino.api.common.resource.base.workorder.enumeration.WorkOrderParticipantType;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.DayShiftType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.WorkOrderType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.StringUtils;
import saleson.model.checklist.Checklist;

@Getter
@Setter
@ToString(exclude = { "ref", "workOrderAssets", "workOrderUsers", "checklist", "moldMaintenance", "picklist" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class WorkOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String workOrderId;

	@Enumerated(EnumType.STRING)
	private WorkOrderType orderType;
	@Enumerated(EnumType.STRING)
	private WorkOrderStatus status;

	@Enumerated(EnumType.STRING)
	private PriorityType priority;

	private String refCode;

	@JsonIgnore
	@OneToMany(mappedBy = "workOrder")
	private List<WorkOrderAsset> workOrderAssets = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "workOrder")
	private List<WorkOrderUser> workOrderUsers = new ArrayList<>();

	private String details;

	@Column(name = "CHECKLIST_ID", insertable = false, updatable = false)
	private Long checklistId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private Checklist checklist;

	@Column(name = "MOLD_MAINTENANCE_ID", insertable = false, updatable = false)
	private Long moldMaintenanceId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private MoldMaintenance moldMaintenance;

	@Lob
	private String checklistItemStr;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean cost;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean pickingList;

	@Column(name = "PICKLIST_ID", insertable = false, updatable = false)
	private Long picklistId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private Checklist picklist;
	@Lob
	private String picklistItemStr;

	private Instant start;
	private Instant end;

	@Enumerated(EnumType.STRING)
	private Frequent frequent;

	@Enumerated(EnumType.STRING)
	private DayShiftType repeatOn;

	private Integer endAfter;

	// for CM
	private Instant failureTime;
	private Integer numberOfBackups;
	private Double costEstimate;
	private String costDetails;
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean cmRequested;
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean approved;

	private Instant startedOn;
	private Instant completedOn;

	@Lob
	private String note;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean requestChange;
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean requestChangeApproved;
	private Long originalId;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean requestApproval;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean requestApprovalEnabled = true;

	private Long estimatedExtendedLifeShot;

	@Column(name = "REJECTED_BY_ID", insertable = false, updatable = false)
	private Long rejectedById;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REJECTED_BY_ID")
	private User rejectedBy;

	private String rejectedReason;

	@Lob
	private String rejectedChangeReason;
	@Lob
	private String declinedReason;
	@Lob
	private String cancelledReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REJECTED_CHANGE_BY_ID")
	private User rejectedChangeBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DECLINED_BY_ID")
	private User declinedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CANCELED_BY_ID")
	private User cancelledBy;

	// accumulated shot when requesting work order CM, Refurbishment, Disposal
	private Integer reportFailureShot;

	private Integer startWorkOrderShot;

//	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	public String getCreatedDate() {
		return DateUtils2.format(getCreatedAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getCreatedDateTime() {
		return DateUtils2.format(getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
	}

	public String getUpdatedDate() {
		return DateUtils2.format(getUpdatedAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getUpdatedDateTime() {
		return DateUtils2.format(getUpdatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
	}

	public List<String> getChecklistItems() {
		List<String> checklistItems = new ArrayList<>();
		if (!StringUtils.isEmpty(checklistItemStr)) {
			String[] itemValues = checklistItemStr.split("\n");
			checklistItems = Arrays.stream(itemValues).collect(Collectors.toList());
		}
		return checklistItems;
	}

	public void setChecklistItems(List<String> checklistItems) {
		this.checklistItemStr = CollectionUtils.isNotEmpty(checklistItems) ? String.join("\n", checklistItems) : null;
	}

	public List<String> getPicklistItems() {
		List<String> picklistItems = new ArrayList<>();
		if (!StringUtils.isEmpty(picklistItemStr)) {
			String[] itemValues = picklistItemStr.split("\n");
			picklistItems = Arrays.stream(itemValues).collect(Collectors.toList());
		}
		return picklistItems;
	}

	public void setPicklistItems(List<String> picklistItems) {
		this.picklistItemStr = CollectionUtils.isNotEmpty(picklistItems) ? String.join("\n", picklistItems) : null;
	}

	public List<Long> getCreatorIds() {
		List<Long> creatorIds = getWorkOrderUsers()
				.stream()
				.filter(wou -> WorkOrderParticipantType.CREATOR.equals(wou.getParticipantType()))
				.map(WorkOrderUser::getUserId)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(creatorIds))
			creatorIds = Collections.singletonList(getCreatedBy());
		return creatorIds;
	}

	public List<User> getAssignees() {
		return getWorkOrderUsers()
				.stream()
				.filter(wou -> WorkOrderParticipantType.ASSIGNEE.equals(wou.getParticipantType()))
				.map(WorkOrderUser::getUser)
				.collect(Collectors.toList());
	}

	public List<Long> getAssigneeIds() {
		return getWorkOrderUsers()
				.stream()
				.filter(wou -> WorkOrderParticipantType.ASSIGNEE.equals(wou.getParticipantType()))
				.map(WorkOrderUser::getUserId)
				.collect(Collectors.toList());
	}
}
