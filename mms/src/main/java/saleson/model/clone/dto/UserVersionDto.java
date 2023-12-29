package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVersionDto {

    private Long id;
    private String name;
    private String email;
    private String department;
    private String position;
    private String contactDialingCode;
    private String contactNumber;
    private String mobileDialingCode;
    private String mobileNumber;
    private String memo;
    private String enabled;
    private Boolean admin;
//    private Boolean requested;
    private Long companyId;
    private String roleIds;
    private String alertStatus;
    private Boolean accessRequest;
    private String companyName;
    private String accessLevel;
    private List<String> accessFeature;
    private List<String> accessGroup;
}
