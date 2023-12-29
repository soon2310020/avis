package saleson.api.suppliersourcing.payload;

import java.util.List;

import com.emoldino.framework.dto.ListOut;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierSourcingGetOut extends ListOut<SupplierSourcingItem> {
	private long totalProductionDemand;
	private long unfulfilledDemand;

	public SupplierSourcingGetOut(List<SupplierSourcingItem> content) {
		super(content);
	}
}
