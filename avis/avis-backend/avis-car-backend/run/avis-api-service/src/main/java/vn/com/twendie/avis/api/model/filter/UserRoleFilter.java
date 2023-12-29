package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRoleFilter {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_in")
    private List<Integer> idIn;

    public UserRoleFilter(Long id) {
        this.id = id;
    }

}
