package saleson.api.category.payload;

import com.emoldino.api.analysis.resource.base.production.dto.ProdBarChart;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;
import saleson.model.Category;
import saleson.model.FileStorage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildProfile {
	private Long id;
	private Long parentId;
	private String parentName;
	private String name;

	private int productCount;
	private int partCount;
	private int supplierCount;
	private int moldCount;
	private Long grandParentId;
	private String grandParentName;

	private long totalProduced;
	private long totalMaxCapacity;
	private long predictedQuantity;
	private long totalProductionDemand;
	private PriorityType deliveryRiskLevel;
	private ProdBarChart productionChart;

	private Long weeklyProductionDemand = 0L;
	private Long specificWeeklyProductionDemand;
	private Long weeklyMaxCapacity = 0L;

	private String projectImagePath;
	private FileStorage projectImage;

	private Boolean isNoBrand = false;

	public ChildProfile(Category product) {
		this.id = product.getId();
		this.name = product.getName();
		this.parentName = product.getParent() != null ? product.getParent().getName() : null;
		this.parentId = product.getParent() != null ? product.getParent().getId() : null;
		this.grandParentName = product.getGrandParent() != null ? product.getGrandParent().getName() : null;
		this.grandParentId = product.getGrandParent() != null ? product.getGrandParent().getId() : null;
		this.weeklyProductionDemand = ValueUtils.toLong(product.getWeeklyProductionDemand(), 0L);
		this.weeklyMaxCapacity = ValueUtils.toLong(product.getWeeklyMaxCapacity(), 0L);
	}
}
