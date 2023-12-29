package saleson.api.dataCompletionRate.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCompletionRatePayload {
    private ObjectType objectType;
    private List<Long> companyId;
    private boolean groupByCompany=false;
    private boolean uncompletedData=false;
    private List<Long> ids = new ArrayList<>();
    private CompanyType companyType;
    private List<String> deletedFields = new ArrayList<>();
    private Boolean isDashboard;
    private Boolean isDataRequest;
    private Long dataRequestId;
    private Long objectId;
    private String query;
    private Boolean ignoreDashboardFilter;
}
