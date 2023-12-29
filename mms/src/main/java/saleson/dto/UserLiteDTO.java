package saleson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.util.Md5Utils;
import saleson.model.User;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLiteDTO {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String position;
    private String contactDialingCode;
    private String contactNumber;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;
    private String companyTypeText;
    private Long companyId;
    private String companyCode;
    private String companyName;

    public UserLiteDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.position = user.getPosition();
        this.contactDialingCode = user.getContactDialingCode();
        this.contactNumber = user.getContactNumber();
        if (user.getCompany() != null){
            this.companyType = user.getCompany().getCompanyType();
            this.companyTypeText = this.companyType != null ? this.companyType.getTitle() : null;
            this.companyId =  user.getCompany().getId();
            this.companyCode=  user.getCompany().getCompanyCode();
            this.companyName=  user.getCompany().getName();

        }
    }
    public String getGravatar() {
        return "//www.gravatar.com/avatar/" + Md5Utils.md5Hex(email) + "?d=mm";
    }

}
