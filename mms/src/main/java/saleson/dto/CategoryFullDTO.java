package saleson.dto;

import com.emoldino.api.analysis.resource.base.production.dto.ProdBarChart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.PriorityType;
import saleson.model.Category;
import saleson.model.customField.CustomFieldValue;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFullDTO {

    private Long id;
    private Long parentId;
    private String parentName;
    private Long grandParentId;
    private String grandParentName;
    private String name;
    private String description;
    private Integer level;
    private Integer sortOrder;
    private boolean enabled;
    private Long parent;
    private List<CategoryFullDTO> children = new ArrayList<>();
    private Integer partCount = 0;
    private Integer supplierCount = 0;
    private Integer moldCount = 0;
    private Long totalProduced = 0L;
    private Long predictedQuantity = 0L;
    private Long totalProductionDemand = 0L;
    private Long totalMaxCapacity = 0L;
    private PriorityType deliveryRiskLevel;
    private double deliverableRate = 1;
    private ProdBarChart productionChart;
    private Long weeklyProductionDemand = 0L;
    private Long weeklyMaxCapacity = 0L;
    private String division;

    private Map<Long,List<CustomFieldValue>> customFieldValueMap=new HashMap<>();

    public CategoryFullDTO(Category category) {
        this.id = category.getId();
        this.parentId = category.getParentId();
        if (category.getParentId() != null) {
            this.parentName = category.getParent().getName();
        }
        if (category.getParent() != null && category.getParent().getParent() != null) {
            this.grandParentId = category.getParent().getParent().getId();
            this.grandParentName = category.getParent().getParent().getName();
        } else if (category.getGrandParent() != null) {
            this.grandParentId = category.getGrandParent().getId();
            this.grandParentName = category.getGrandParent().getName();
        }
        this.name = category.getName();
        this.description = category.getDescription();
        this.level = category.getLevel();
        this.sortOrder = category.getSortOrder();
        this.enabled = category.isEnabled();
        this.parent = category.getParentId();
        this.partCount = category.getPartCount();
        this.supplierCount = category.getSupplierCount();
        this.moldCount = category.getMoldCount();
        this.totalProduced = category.getTotalProduced();
        this.predictedQuantity = category.getPredictedQuantity();
        this.totalProductionDemand = category.getTotalProductionDemand();
        this.totalMaxCapacity = category.getTotalMaxCapacity();
        this.deliveryRiskLevel = category.getDeliveryRiskLevel();
        this.deliverableRate = category.getDeliverableRate();
        this.productionChart = category.getProductionChart();
        this.weeklyProductionDemand = category.getWeeklyProductionDemand();
        this.weeklyMaxCapacity = category.getWeeklyMaxCapacity();
        this.division = category.getDivision();
    }
}
