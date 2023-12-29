package saleson.api.suppliersourcing.payload;

import com.emoldino.framework.dto.ListIn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SupplierSourcingPostIn extends ListIn<SupplierSourcingItem> {
	private Long productId;
	private Long partId;
	private String periodType;
	private String periodValue;
}
