package saleson.model.clone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.AlertType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.User;
import saleson.model.support.VersionAudit;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Size(max = 40)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private String department;
    private String position;
    private String contactDialingCode;
    private String contactNumber;

    private String mobileDialingCode;
    private String mobileNumber;

    @Lob
    private String memo;

    private String enabled;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean admin;

    private Long companyId;
    private String roleIds;

    private String alertStatus;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean accessRequest;

//    @Builder
//    public UserVersion(Long id, String name, String email, String department, String position, String contactDialingCode,
//                       String contactNumber, String mobileDialingCode, String mobileNumber, String memo, Boolean enabled,
//                       Boolean admin, Boolean requested, Long companyId, String roleIds, String alertStatus, Boolean accessRequest){
//        this.id = id;
//        this.name = name;
//        this.email = email;
//        this.department = department;
//        this.position = position;
//        this.contactDialingCode = contactDialingCode;
//        this.contactNumber = contactNumber;
//        this.mobileDialingCode = mobileDialingCode;
//        this.mobileNumber = mobileNumber;
//        this.memo = memo;
//        this.enabled = enabled;
//        this.admin = admin;
//        this.requested = requested;
//        this.companyId = companyId;
//        this.roleIds = roleIds;
//        this.alertStatus = alertStatus;
//        this.accessRequest = accessRequest;
//    }
}
