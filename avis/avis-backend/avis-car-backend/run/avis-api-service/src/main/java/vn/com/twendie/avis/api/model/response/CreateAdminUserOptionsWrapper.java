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
public class CreateAdminUserOptionsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("departments")
    private List<UserDepartmentDTO> userDepartmentDTOS;

    @JsonProperty("branches")
    private List<BranchDTO> branches;

    @JsonProperty("code")
    private String code;

}
