package saleson.api.company.payload;

import lombok.Data;

@Data
public class SupplierGetIn {
	private Long productId;
	private Long partId;
	private Long moldId;
	private String periodType;
	private String periodValue;
}
