package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ContractChangeHistoryItemDTO {

    @JsonProperty("number_order") Integer numberOrder;
    @JsonProperty("created_at") Timestamp createdAt;
    @JsonProperty("field_change") String fieldName;
    @JsonProperty("old_value") String oldValue;
    @JsonProperty("new_value") String newValue;
    @JsonProperty("effective_date") Timestamp effectiveDate;
    @JsonProperty("updated_by") String updatedBy;


}
