package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(indexes = { @Index(name = "IDX_MOLD_PART", columnList = "MOLD_ID, PART_ID", unique = true) })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MoldPart {
	@Id
	@GeneratedValue
	private Long id;

	//@JsonIgnore
	@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private Mold mold;

	@Column(name = "PART_ID", insertable = false, updatable = false)
	private Long partId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private Part part;

	@Transient
	private String partCode;
	@Transient
	private String partName;

	private Integer cavity;
	private Integer totalCavities;

	private Instant switchedTime;

	public MoldPart(Mold mold, Part part) {
		this.mold = mold;
		this.part = part;
	}

	public MoldPart(Mold mold, Part part, Integer cavity, Integer totalCavities) {
		this(mold, part);
		this.cavity = cavity;
		this.totalCavities = totalCavities;
	}

	// 파트명
	public String getPartCode() {
		if (this.part == null && this.partCode == null) {
			return "";
		}
		return part != null ? part.getPartCode() : this.partCode;
	}

	// 파트명
	public String getPartName() {
		if (this.part == null && this.partName == null) {
			return "";
		}
		return part != null ? part.getName() : this.partName;
	}

	public String getResinCode() {
		if (this.part == null) {
			return "";
		}
		return part.getResinCode();
	}

	public String getResinGrade() {
		if (this.part == null) {
			return "";
		}
		return part.getResinGrade();
	}

	// 카테고리명 (1차 - 부모카테고리)
	public String getCategoryName() {
		if (part != null && part.getCategory() != null && part.getCategory().getGrandParent() != null) {
			return part.getCategory().getGrandParent().getName();
		}
		return "";
/*
		if (this.part == null || part.getCategory() == null || part.getCategory().getParent() == null) {
			return "";
		}
		return part.getCategory().getParent().getName();
*/
	}

	public Long getCategoryId() {
		if (part != null && part.getCategory() != null) {
			return part.getCategory().getGrandParentId();
		}
		return null;
/*
		if (this.part == null || part.getCategory() == null || part.getCategory().getParent() == null) {
			return null;
		}
		return part.getCategory().getParent().getId();
*/
	}

	// 프로젝트명 (2차 - 카테고리)
	public String getProjectName() {
		if (this.part == null || part.getCategory() == null) {
			return "";
		}
		return part.getCategory().getName();
	}

	public Long getProjectId() {
		if (this.part == null || part.getCategory() == null) {
			return null;
		}
		return part.getCategory().getId();
	}

	public String getPartSize() {
		return part!=null ? part.getPartSize(): null;
		/*
		if (this.part == null || part.getSize() == null || part.getSize().equalsIgnoreCase("")
				|| part.getSize().equalsIgnoreCase("undefinedxundefinedxundefined")|| part.getSize().equalsIgnoreCase("xx")) return "";
		String sizeUnit = part.getSizeUnit()!=null ? part.getSizeUnit().getTitle():"";
		if (part.getPartSize() != null)
		if(part.getSizeUnit().equals(SizeUnit.MM) || part.getSizeUnit().equals(SizeUnit.CM) || part.getSizeUnit().equals(SizeUnit.M))
			sizeUnit += "³";
		return part.getSize() + " " + sizeUnit;
		*/
	}

	public String getSizeUnit() {
		if (this.part == null || this.part.getSizeUnit() == null) {
			return "";
		}
		return this.part.getSizeUnit().name();
	}

	public String getPartWeight() {
		if (this.part == null || part.getWeight() == null || part.getWeight().equalsIgnoreCase("")) {
			return "";
		}
		return part.getWeight() + (part.getWeightUnit() != null ? " " + part.getWeightUnit().getTitle() : "");
	}

	public String getDesignRevision() {
		if (this.part == null || part.getDesignRevision() == null) {
			return "";
		}
		return part.getDesignRevision();
	}
}
