package saleson.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.BatteryStatus;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.PresetStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.model.support.Equipment;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = { @Index(name = "IDX_COMPANY_ID", columnList = "companyId") })
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonPropertyOrder(alphabetic = true)
public class Counter extends Equipment {
	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "counter")
	private Mold mold;

	private Integer shotCount;
	private Instant lastShotAt; // 마지막 Shot 날짜
	private Instant lastShotAtVal;

	private Long lastTerminalId;

	private Integer presetCount; // Preset 설정 카운터
	@Enumerated(EnumType.STRING)
	private PresetStatus presetStatus;

	@Enumerated(EnumType.STRING)
	private BatteryStatus batteryStatus;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Transient
	private Long lastWorkOrderId;

	@Transient
	private Long workOrderHistory;

	private Integer subscriptionTerm;

	public Counter(Long id) {
		this.id = id;
	}

	public String getMoldStatus() {
		if (mold != null) {
			return mold.getMoldStatus();
		} else {
			return "NO_SENSOR";
		}
	}

	public CounterStatus getCounterStatus() {
		if (mold == null) {
			return CounterStatus.NOT_INSTALLED;
		} else if (getEquipmentStatus() == EquipmentStatus.DETACHED) {
			return CounterStatus.DETACHED;
		}
		return CounterStatus.INSTALLED;
	}

	public String getSubscriptionStatus() {
		if (getActivatedAt() != null && subscriptionTerm != null) {
			if (getActivatedAt().isBefore(Instant.now().minus(subscriptionTerm * 365, ChronoUnit.DAYS))) {
				return "Expired";
			} else {
				return "Valid";
			}
		}
		return "None";
	}

	public Instant getSubscriptionExpiry() {
		if (getActivatedAt() != null && subscriptionTerm != null) {
			Instant subscriptionExpiry = getActivatedAt().plus(subscriptionTerm * 365, ChronoUnit.DAYS);
			return subscriptionExpiry;
		}
		return null;
	}

	public String getActivePeriod() {
		if (getActivatedAt() != null) {
			Instant lastActive = Instant.now();
			if (getSubscriptionExpiry() != null && getSubscriptionExpiry().isBefore(lastActive)) {
				lastActive = getSubscriptionExpiry();
			}
			return DateUtils.getPeriodDate(getActivatedAt(), lastActive);
		}
		return "";
	}
}
