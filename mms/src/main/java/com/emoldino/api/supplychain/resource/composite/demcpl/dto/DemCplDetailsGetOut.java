package com.emoldino.api.supplychain.resource.composite.demcpl.dto;

import java.util.List;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetOut.DemCplDetails;
import com.emoldino.framework.dto.ListOut;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.EquipmentStatus;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemCplDetailsGetOut extends ListOut<DemCplDetails> {
	private List<FltSupplier> suppliers;
	private FltPart part;

	public DemCplDetailsGetOut(List<DemCplDetails> content, List<FltSupplier> suppliers, FltPart part) {
		super(content);
		this.suppliers = suppliers;
		this.part = part;
	}

	public DemCplDetailsGetOut(List<DemCplDetails> content) {
		super(content);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DemCplDetails {
		private Long moldId;
		private String moldCode;
		private ToolingStatus toolingStatus;
		private EquipmentStatus equipmentStatus;

		private Long supplierId;
		private String supplierName;
		private String supplierCode;

		private Double utilizationRate;
		private Double approvedCycleTime;
		private Double averageCycleTime;
		private Long producedQuantity;
		private Long productionCapacity;

		public EquipmentStatus getMoldStauts() {
			return equipmentStatus;
		}

		public DemCplDetails(Long moldId, String moldCode, ToolingStatus toolingStatus, EquipmentStatus equipmentStatus, //
				Long supplierId, String supplierName, String supplierCode, //
				Double utilizationRate, Float approvedCycleTime, Double averageCycleTime, //
				Long producedQuantity, Long productionCapacity) {
			this.moldId = moldId;
			this.moldCode = moldCode;
			this.toolingStatus = toolingStatus;
			this.equipmentStatus = equipmentStatus;
			this.supplierId = supplierId;
			this.supplierName = supplierName;
			this.supplierCode = supplierCode;
			this.utilizationRate = utilizationRate;
			this.approvedCycleTime = approvedCycleTime != null ? approvedCycleTime.doubleValue() : null;
			this.averageCycleTime = averageCycleTime;
			this.producedQuantity = producedQuantity;
			this.productionCapacity = productionCapacity;
		}
	}
}
