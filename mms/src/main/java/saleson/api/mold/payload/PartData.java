package saleson.api.mold.payload;

import lombok.Data;
import saleson.model.customField.CustomFieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PartData {
	private Long categoryId;
	private String categoryName;

	private Long projectId; 		// categoryId
	private String projectName;		// categoryName

	private Long partId;
	private String partCode;
	private String partName;
	private Integer cavity;
	private Integer totalCavities;
	private String resinCode;
	private String resinGrade;
	private String partVolume;
	private String partWeight;
	private String designRevision;
	private Integer weeklyDemand;
	private Long quantityRequired;
	private Map<Long, List<CustomFieldValue>> customFieldValueMap=new HashMap<>();

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getPartId() {
		return partId;
	}

	public void setPartId(Long partId) {
		this.partId = partId;
	}

	public String getPartCode() {
		return partCode;
	}

	public void setPartCode(String partCode) {
		this.partCode = partCode;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public Integer getCavity() {
		return cavity;
	}

	public void setCavity(Integer cavity) {
		this.cavity = cavity;
	}

	public String getResinCode() { return resinCode; }

	public void setResinCode(String resinCode) { this.resinCode = resinCode; }

	public String getResinGrade() { return resinGrade; }

	public void setResinGrade(String resinGrade) { this.resinGrade = resinGrade; }

	public String getPartVolume() { return partVolume; }

	public void setPartVolume(String partVolume) { this.partVolume = partVolume; }

	public String getPartWeight() { return partWeight; }

	public void setPartWeight(String partWeight) { this.partWeight = partWeight; }

	public String getDesignRevision(){ return designRevision; }

	public void setDesignRevision(String designRevision){ this.designRevision = designRevision; }
}
