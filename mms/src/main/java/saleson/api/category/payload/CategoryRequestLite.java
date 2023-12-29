package saleson.api.category.payload;

import java.util.List;

import lombok.Data;
import saleson.common.util.StringUtils;
import saleson.model.Category;

@Data
public class CategoryRequestLite {
	private Long id;
	private String name;
	private String description;
	private Long parentId;
	private Integer level;

	private Long grandParentId;
	private String division;
	private List<Long> childIds;
	private boolean enabled;
	private Long weeklyProductionDemand;
	private List<String> periodValue;
	private long specificWeeklyProductionDemand;

	@Deprecated
	public void setProductionDemand(Long weeklyProductionDemand) {
		this.weeklyProductionDemand = weeklyProductionDemand;
	}

	public Category getModel() {
		Category category = new Category();
		category.setName(StringUtils.trimWhitespace(name));
		category.setDescription(StringUtils.trimWhitespace(description));
		category.setDivision(division);
		category.setParentId(parentId);
		category.setGrandParentId(grandParentId);
		category.setEnabled(enabled);
		category.setWeeklyProductionDemand(weeklyProductionDemand);

		return category;
	}
	public Category getModel(Category category) {
		bindData(category);
		return category;
	}
	private void bindData(Category category){
		category.setName(StringUtils.trimWhitespace(name));
		category.setDescription(StringUtils.trimWhitespace(description));
		category.setDivision(division);
		category.setParentId(parentId);
//		category.setGrandParentId(grandParentId);
		category.setEnabled(enabled);
		category.setWeeklyProductionDemand(weeklyProductionDemand);

	}

	public Category getModelFull() {
		Category category = new Category();
		category.setId(id);
		category.setName(StringUtils.trimWhitespace(name));
		category.setDescription(StringUtils.trimWhitespace(description));
		category.setDivision(division);
		category.setParentId(parentId);
		category.setGrandParentId(grandParentId);
		category.setEnabled(enabled);
		category.setWeeklyProductionDemand(weeklyProductionDemand);
		category.setLevel(level);
		return category;
	}
}
