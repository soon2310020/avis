package saleson.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.Equipment;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = { @Index(name = "IDX_COMPANY_ID", columnList = "companyId") })
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Terminal extends Equipment {
	@Id
	@GeneratedValue
	private Long id;

	private String installationArea;
	private IpType ipType;

	private String ipAddress;
	private String subnetMask;
	private String gateway;
	private String dns;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Transient
	private Long lastWorkOrderId;

	@Transient
	private Long workOrderHistory;

	public String getConnectionStatus() {
		return getOperatingStatus() == OperatingStatus.WORKING ? "ONLINE" : "OFFLINE";
	}
}
