package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminUserDTO {

    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("department")
    private UserDepartmentDTO department;

    @JsonProperty("branch")
    private BranchDTO branch;

    @JsonProperty("user_role")
    private UserRoleDTO userRole;

    @JsonProperty("created_by")
    private UserDTO createdBy;

    @JsonProperty("removable")
    private Boolean removable;

    @JsonProperty("created_at")
    private Timestamp createdAt;

}
