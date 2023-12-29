package saleson.api.category.payload;

import java.util.List;

import lombok.Data;

@Data
public class ProfileFilter {
	private Long projectId;
	private Long brandId;
	private Long partId;
	private Long moldId;
	private List<Long> supplierId;
	private String periodType;
	private String periodValue;
}
