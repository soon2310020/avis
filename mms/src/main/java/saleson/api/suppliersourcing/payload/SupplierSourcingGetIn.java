package saleson.api.suppliersourcing.payload;

import lombok.Data;

@Data
public class SupplierSourcingGetIn {
	private Long productId;
	private Long partId;
	private String periodType;
	private String periodValue;
}
