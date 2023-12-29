package saleson.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

@Deprecated
@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DashboardGeneralFilter extends DateAudit {
	@Id
	@GeneratedValue
	private Long id;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	private User user;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DASHBOARD_GENERAL_FILTER_PART", joinColumns = @JoinColumn(name = "DASHBOARD_GENERAL_FILTER_ID"), inverseJoinColumns = @JoinColumn(name = "PART_ID"))
	private List<Part> parts;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DASHBOARD_GENERAL_FILTER_SUPPLIER", joinColumns = @JoinColumn(name = "DASHBOARD_GENERAL_FILTER_ID"), inverseJoinColumns = @JoinColumn(name = "SUPPLIER_ID"))
	private List<Company> suppliers;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DASHBOARD_GENERAL_FILTER_TOOLMAKER", joinColumns = @JoinColumn(name = "DASHBOARD_GENERAL_FILTER_ID"), inverseJoinColumns = @JoinColumn(name = "TOOLMAKER_ID"))
	private List<Company> toolMakers;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DASHBOARD_GENERAL_FILTER_LOCATION", joinColumns = @JoinColumn(name = "DASHBOARD_GENERAL_FILTER_ID"), inverseJoinColumns = @JoinColumn(name = "LOCATION_ID"))
	private List<Location> locations;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean allPart;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean allSupplier;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean allToolMaker;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean allLocation;

	public DashboardGeneralFilter(boolean allPart, boolean allSupplier, boolean allToolMaker, boolean allLocation) {
		this.allPart = allPart;
		this.allSupplier = allSupplier;
		this.allToolMaker = allToolMaker;
		this.allLocation = allLocation;
	}
}
