package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.RoleType;
import saleson.common.util.DataUtils;
import saleson.model.User;
import saleson.model.UserAlert;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    @JsonProperty("company")
    private String companyName;

    private String department;
    private String position;

    @JsonProperty("contact_dialing_code")
    private String contactDialingCode;
    @JsonProperty("contact_number")
    private String contactNumber;
    @JsonProperty("mobile_dialing_code")
    private String mobileDialingCode;
    @JsonProperty("mobile")
    private String mobileNumber;
    //access
    @JsonProperty("access_enabled")
    private boolean enabled;
    @JsonProperty("access_level")
    private String accessLevel;
    @JsonProperty("access_request")
    private Boolean accessRequest;

    @JsonProperty("access_feature")
    private List<String> accessFeature;
    @JsonProperty("access_group")
    private List<String> accessGroup;

    //email notification
    @JsonProperty("alert_corrective_maintenance")
    private Boolean alertCorrectiveMaintenance;
    @JsonProperty("alert_preventive_maintenance")
    private Boolean alertPreventiveMaintenance;
    @JsonProperty("alert_uptime")
    private Boolean alertUptime;
    @JsonProperty("alert_cycle_time")
    private Boolean alertCycleTime;
    @JsonProperty("alert_reset")
    private Boolean alertReset;
    @JsonProperty("alert_disconnection")
    private Boolean alertDisconnection;
    @JsonProperty("alert_relocation")
    private Boolean alertRelocation;


    private String memo;
    private boolean admin;

    private String token;

    public UserDTO(User user, String token) {
        this.setId(user.getId());
        this.setEmail(user.getEmail());
        this.setMobileNumber(user.getMobileNumber());
        this.setAdmin(user.isAdmin());
        this.setName(user.getName());
        this.token = token;
    }

    public static UserDTO convertToDTO(User user,List<UserAlert> userAlertList) {
        UserDTO userDTO = DataUtils.mapper.map(user, UserDTO.class);
        userDTO.setAccessLevel(user.isAdmin() ? "admin" : "regular");
        if (user.getRoles() != null) {
            List<String> accessFeatures = new ArrayList<>();
            List<String> accessGroups = new ArrayList<>();
            user.getRoles().forEach(role -> {
                if (RoleType.ROLE_MENU.equals(role.getRoleType()) && role.getName() != null) {
                    accessFeatures.add(role.getName());
                }
                if (RoleType.ROLE_GROUP.equals(role.getRoleType()) && role.getName() != null) {
                    accessGroups.add(role.getName());
                }
            });
            userDTO.setAccessFeature(accessFeatures);
            userDTO.setAccessGroup(accessGroups);
        }
        if(userAlertList!=null){
            userAlertList.forEach(userAlert -> {
                if(AlertType.CYCLE_TIME.equals(userAlert.getAlertType())){
                    userDTO.setAlertCycleTime(true);
                }else if(AlertType.CORRECTIVE_MAINTENANCE.equals(userAlert.getAlertType())){
                    userDTO.setAlertCorrectiveMaintenance(true);
                }else if(AlertType.MAINTENANCE.equals(userAlert.getAlertType())){
                    userDTO.setAlertPreventiveMaintenance(true);
                }else if(AlertType.EFFICIENCY.equals(userAlert.getAlertType())){
                    userDTO.setAlertUptime(true);
                }else if(AlertType.MISCONFIGURE.equals(userAlert.getAlertType())){
                    userDTO.setAlertReset(true);
                }else if(AlertType.DISCONNECTED.equals(userAlert.getAlertType())){
                    userDTO.setAlertDisconnection(true);
                }else if(AlertType.RELOCATION.equals(userAlert.getAlertType())){
                    userDTO.setAlertRelocation(true);
                }else if(AlertType.COUNTER_MISWORKING.equals(userAlert.getAlertType())){
//                    userDTO.setAlertCou(true);
                }else if(AlertType.DATA_SUBMISSION.equals(userAlert.getAlertType())){
//                    userDTO.setAlertD(true);
                }else if(AlertType.REFURBISHMENT.equals(userAlert.getAlertType())){
//                    userDTO.setAlert(true);
                }else if(AlertType.TERMINAL_DISCONNECTED.equals(userAlert.getAlertType())){
//                    userDTO.setAlert(true);
                }else if(AlertType.TOOL_MISWORKING.equals(userAlert.getAlertType())){
//                    userDTO.setAlert(true);
                }

            });
        }

        return userDTO;
    }
}
