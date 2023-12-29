package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFilter {

    @JsonProperty("id_in")
    private List<Integer> idIn;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("branch")
    private BranchFilter branch;

    @JsonProperty("department")
    private DepartmentFilter department;

    @JsonProperty("user_role")
    private UserRoleFilter userRole;

    @JsonProperty("created_by")
    private CreatedByFilter createdBy;

    @JsonProperty("created_at")
    private Timestamp createdAt;

}
