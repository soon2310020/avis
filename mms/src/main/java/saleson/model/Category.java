package saleson.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.emoldino.api.analysis.resource.base.production.dto.ProdBarChart;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.PriorityType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
public class Category extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "PARENT_ID", insertable = false, updatable = false)
	private Long parentId;

	private String name;
	private String description;

	private Integer level;
	private Integer sortOrder;

	@Column(length = 1, nullable = false)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	//@JsonIgnore
	//@Where(clause = "ENABLED='Y'")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID")
	private Category parent;

	//@Where(clause = "ENABLED='Y'")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID")
	private List<Category> children = new ArrayList<>();//chilren of branh

	@Transient
	private int partCount;
	@Transient
	private int supplierCount;
	@Transient
	private int moldCount;

	@Transient
	private long totalProduced;
	@Transient
	private long predictedQuantity;
	@Transient
	private long totalProductionDemand;
	@Transient
	private long totalMaxCapacity;
	@Transient
	private PriorityType deliveryRiskLevel;
	@Transient
	private double deliverableRate = 1;
	@Transient
	private ProdBarChart productionChart;

	private Long weeklyProductionDemand = 0L;
	@Transient
	private Long weeklyMaxCapacity = 0L;

	@Column(name = "GRAND_PARENT_ID", insertable = false, updatable = false)
	private Long grandParentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GRAND_PARENT_ID")
	private Category grandParent;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "GRAND_PARENT_ID")
	private List<Category> grandchildren = new ArrayList<>();//chilren of category

	private String division;
}
