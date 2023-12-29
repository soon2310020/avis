package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JDDFilterOptionsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("branches")
    private List<BranchDTO> branchDTOS;

    @JsonProperty("contract_types")
    private List<ContractTypeDTO> contractTypeDTOS;

    @JsonProperty("diaries_lock")
    private JourneyDiaryDailyLockDTO journeyDiaryDailyLockDTO;

}
