package saleson.api.user.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.Company;
import saleson.model.User;
import saleson.model.UserInvite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitePayload {
    private List<UserInvite> userInviteList;

    public List<UserInvite> getModels(){
        List<UserInvite> userInviteListClone = new ArrayList<>();
        if(userInviteList!=null){
            userInviteList.forEach(ui->{
                ui.setEmail(StringUtils.trimWhitespace(ui.getEmail()));
                if(!StringUtils.isEmpty(ui.getEmail())){
                    userInviteListClone.add(ui);
                }else if (!StringUtils.isEmpty(ui.getEmailList())){
                    ui.getEmailList().forEach(email->{
                        if(!StringUtils.isEmpty(email)){
                            UserInvite userInvite = UserInvite.builder()
                                    .email(email)
                                    .companyId(ui.getCompanyId())
                                    .build();
                            userInviteListClone.add(userInvite);
                        }
                    });
                }
            });
        }
        return userInviteListClone;
    }
}
