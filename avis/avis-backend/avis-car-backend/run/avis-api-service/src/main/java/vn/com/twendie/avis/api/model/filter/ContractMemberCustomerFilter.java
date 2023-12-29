package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor
public class ContractMemberCustomerFilter {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("id_in")
    private List<Integer> idIn;

}
