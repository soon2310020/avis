package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.data.model.Branch;

import java.sql.Timestamp;

import static vn.com.twendie.avis.data.enumtype.UserRoleEnum.DRIVER;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverFilter {

    @JsonProperty("name")
    private String name;

    @JsonProperty("id_card")
    private String idCard;

    @JsonProperty("birthdate")
    private Timestamp birthdate;

    @JsonProperty("mobile_full")
    private String mobileFull;

    @JsonProperty("user_group")
    private UserGroupFilter userGroup;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonIgnore
    private Branch branch;

    @JsonProperty("status")
    private Integer status;
    @JsonIgnore
    private UserRoleFilter userRole = new UserRoleFilter(DRIVER.getId());

}
