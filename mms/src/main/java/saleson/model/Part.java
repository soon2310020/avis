package saleson.model;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;

import com.emoldino.api.analysis.resource.base.production.dto.ProdBarChart;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.customField.CustomFieldValue;
import saleson.model.data.MiniComponentData;
import saleson.model.data.StatisticsPartData;
import saleson.model.support.UserDateAudit;

@Entity
@Getter
@Setter
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" }, ignoreUnknown=true)
@JsonPropertyOrder(alphabetic = true)
public class Part extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	private String name;
	private String partCode;
	@Lob
	private String resinCode;
	private String resinGrade;
	private String designRevision;
	private Integer weeklyDemand;

	private String size;
	@Enumerated(EnumType.STRING)
	private SizeUnit sizeUnit;

	private String weight;
	@Enumerated(EnumType.STRING)
	private WeightUnit weightUnit;

	private Integer price;
	@Enumerated(EnumType.STRING)
	private CurrencyType currencyType;

	private String downstreamSite;
	private String memo;

	private Instant dataUpdatedOn;

	@Column(name = "CATEGORY_ID", insertable = false, updatable = false)
	private Long categoryId;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORY_ID")
	private Category category;

	@JsonIgnore
	@OneToMany(mappedBy = "part")
	private Set<MoldPart> moldParts = new HashSet<>();

	/*@OneToMany(fetch = FetchType.LAZY, mappedBy = "part")
	private List<Mold> molds;*/

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	/*
		@OneToMany(mappedBy = "part")
		private Set<MoldPart> moldParts = new HashSet<>();
	*/

	private Long quantityRequired;

	@Transient
	private StatisticsPartData statisticsPartData;
	//customize value
	@Transient
	private Map<Long, List<CustomFieldValue>> customFieldValueMap = new HashMap<>();

	public Part() {
	}

	public Part(Long id) {
		this.id = id;
	}

	public Long getSupplierCount() {
		if (moldParts == null) {
			return 0L;
		}
		return (long) moldParts.stream().map(m -> m.getMold().getCompanyId()).collect(Collectors.toSet()).size();
	}

	// 화면에 제공할 데이터
	public Long getMoldCount() {
		return getMoldCounter(null);
	}

	public Long getActiveMolds() {
		return getMoldCounter(OperatingStatus.WORKING);
	}

	public Long getIdleMolds() {
		return getMoldCounter(OperatingStatus.IDLE);
	}

	public Long getInactiveMolds() {
		return getMoldCounter(OperatingStatus.NOT_WORKING);
	}

	public Long getDisconnectedMolds() {
		return getMoldCounter(OperatingStatus.DISCONNECTED);
	}

	private Long getMoldCounter(OperatingStatus operatingStatus) {

		if (moldParts == null)
			return 0L;

		if (operatingStatus == null) {
			return moldParts.stream()
//					.filter(mp -> mp.getMold().getEquipmentStatus() != EquipmentStatus.DISCARDED)
					.count();
		}
		return moldParts.stream().filter(mp -> mp.getMold().getOperatingStatus() == operatingStatus)
//						&& mp.getMold().getEquipmentStatus() != EquipmentStatus.DISCARDED)
				.count();
	}

	// 카테고리명 (1차 - 부모카테고리)
	public String getCategoryName() {
		if (this.getCategory() != null && this.getCategory().getGrandParent() != null) {
			return this.getCategory().getGrandParent().getName();
		}
		return "";

/*
		if (getCategory() != null && getCategory().getParent() != null && getCategory().getParent().getParent() != null) {
			return getCategory().getParent().getParent().getName();
		}
		if (getCategory() != null && getCategory().getGrandParent() != null) {
			return getCategory().getGrandParent().getName();
		}

		return "";
*/
	}

	// 프로젝트명 (2차 - 카테고리)
	public String getProjectName() {
		if (getCategory() == null)
			return "";

		return getCategory().getName();
	}

	/**
	 * 생산 수량 (통계 데이터 기준)
	 *
	 * @return
	 */
	public int getTotalProduced() {
		if (producedQuantity != null) {
			return producedQuantity.intValue();
		}

		if (getStatisticsPartData() == null) {
			return 0;
		}

		return ValueUtils.toInteger(getStatisticsPartData().getProducedQuantity(), 0);
		/*
		if (moldParts == null) return 0L;

		long quantity = 0L;
		for (MoldPart moldPart : moldParts) {
			if (moldPart.getMold().getLastShot() == null
					|| moldPart.getCavity() == null) {
				continue;
			}
			quantity += (long) (moldPart.getMold().getLastShot() * moldPart.getCavity());
		}
		return quantity;
		*/
	}

	@Transient
	private Long producedQuantity;
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

	// partSize
	public String getPartSizeNoUnit() {
		if (size == null || size.equalsIgnoreCase("") || size.equalsIgnoreCase("undefinedxundefinedxundefined") || size.equalsIgnoreCase("xx"))
			return "";
		size = size.replaceAll("undefined", "0");
		if (size.startsWith("x"))
			size = "0" + size;
		size = size.replaceAll("x\\s*x", "x 0 x");
		if (size.endsWith("x"))
			size = size + "0";
		size = size.replaceAll("x", " x ").replaceAll("\\s+", " ").trim();

		return size;
	}

	public String getPartSize() {
		String sizeValue = getPartSizeNoUnit();
		if (StringUtils.isEmpty(sizeValue))
			return sizeValue;

		String unit = getSizeUnit() != null ? getSizeUnit().getTitle() : "";
		if (getSizeUnit() != null) {
			if (getSizeUnit().equals(SizeUnit.MM) || getSizeUnit().equals(SizeUnit.CM) || getSizeUnit().equals(SizeUnit.M))
				unit += "³";
		}
		return sizeValue + " " + unit;
	}

	// partWeight
	public String getPartWeight() {
		if (weight == null || weight.equalsIgnoreCase(""))
			return "";
		return weight + " " + (weightUnit != null ? weightUnit.getTitle() : "");
	}

	// partPrice
	public String getPartPrice() {
		if (price == null)
			return "";
		return price + "" + (currencyType != null ? currencyType.getTitle() : "");
	}

	public String getDataUpdatedDate() {
		return DateUtils.getDate(getDataUpdatedOn());
	}

	public String getDataUpdatedDateTime() {
		return DateUtils.getDateTime(getDataUpdatedOn());
	}

	public Integer getTotalMolds() {
		return CollectionUtils.isEmpty(moldParts) ? 0 : moldParts.size();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public List<MiniComponentData> getMachineList() {
		List<MiniComponentData> machineList = Lists.newArrayList();
		if(CollectionUtils.isNotEmpty(moldParts)) {
			machineList = moldParts.stream()
					.map(MoldPart::getMold)
					.map(Mold::getMachine)
					.filter(Objects::nonNull)
					.map(machine -> new MiniComponentData(machine.getId(), machine.getMachineCode()))
					.collect(Collectors.toList());
		}
		return machineList;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public List<MiniComponentData> getMolds() {
		if (!CollectionUtils.isEmpty(moldParts))
			return moldParts.stream().map(mp -> new MiniComponentData(mp.getMold())).collect(Collectors.toList());
		else return null;
	}
}
