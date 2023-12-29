package saleson.api.suppliersourcing.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PriorityType;

@Data
@NoArgsConstructor
public class SupplierSourcingItem {
	private Long id;
	private String name;
	private long moldCount;
	private long totalProduced;
	private long predictedQuantity;
	private long totalMaxCapacity;
	private long remainingCapacity;
	private long totalProductionDemand;
	private PriorityType deliveryRiskLevel;
	private double deliverableRate = 1;

	public SupplierSourcingItem(Long id, String name, long totalProductionDemand) {
		this(id, name, 0L, 0L, 0L, 0L, 0L, totalProductionDemand);
	}

	public SupplierSourcingItem(Long id, String name, long moldCount, long totalProduced, long predictedQuantity, long totalMaxCapacity) {
		this(id, name, moldCount, totalProduced, predictedQuantity, totalMaxCapacity, 0L, 0L);
	}

	private SupplierSourcingItem(Long id, String name, long moldCount, long totalProduced, long predictedQuantity, long totalMaxCapacity, long remainingCapacity,
			long totalProductionDemand) {
		this.id = id;
		this.name = name;
		this.moldCount = moldCount;
		this.totalProduced = totalProduced;
		this.predictedQuantity = predictedQuantity;
		this.totalMaxCapacity = totalMaxCapacity;
		this.remainingCapacity = remainingCapacity;
		this.totalProductionDemand = totalProductionDemand;
	}
}
