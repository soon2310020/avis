package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractCustomerFilter {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("id_in")
    private List<Integer> idIn;

    @JsonProperty("name")
    private String name;

}
