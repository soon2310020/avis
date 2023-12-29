package saleson.api.report.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MoldStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.enumeration.productivity.ProductivityVariable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductivitySearchPayload {
	private ProductivityVariable variable;
	private Long partId;
	private List<String> countryCode;
	private String startDate;
	private String endDate;
	private Integer duration; // number of days in time period filter
	private List<OperatingStatus> operatingStatus;

	//for supplier
	private boolean groupBySuppliers;
	private boolean checkForNewCounter;
	private CompareType compareBy;

	//new condition
	private List<Long> supplierIds;// user to company off location

	//for tooltip in cycle time report
	private boolean forTooltip;
	private boolean compareBySupplier;
	private boolean compareByToolMaker;
	private Long companyId;

	private List<MoldStatus> moldStatusList;

}
