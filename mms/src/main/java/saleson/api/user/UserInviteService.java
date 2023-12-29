package saleson.api.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.company.CompanyService;
import saleson.api.notification.NotificationService;
import saleson.api.user.payload.UserInvitePayload;
import saleson.api.user.payload.UserPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.config.Const;
import saleson.common.notification.MailService;
import saleson.common.payload.ApiResponse;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.User;
import saleson.model.UserInvite;
import saleson.model.data.errorData.ErrorIndexData;
import saleson.service.mail.InviteUserEmailContent;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserInviteService {
    @Autowired
    UserInviteRepository userInviteRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MailService mailService;
    @Autowired
    CompanyService companyService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    InviteUserEmailContent inviteUserEmailContent;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    VersioningService versioningService;
    @Value("${host.url}")
    String host;

    public ApiResponse validInviteUser(UserInvitePayload payload){
        if(payload == null || payload.getUserInviteList() == null || payload.getUserInviteList().size() == 0){
            return ApiResponse.error(Const.ERROR_CODE.NO_DATA);
        }
        List<ErrorIndexData> errorData = new ArrayList<>();
        payload.getUserInviteList().forEach(invitation -> {
            if(invitation.getEmail() != null){
                if(userRepository.existsByEmailAndDeletedIsFalse(invitation.getEmail())){
                    errorData.add(ErrorIndexData.builder()
                            .index(invitation.getIndex())
                            .data(Arrays.asList(invitation.getEmail()))
                            .build());
                }
            }else if(invitation.getEmailList() != null){
                List<String> errorEmails = invitation.getEmailList().stream()
                        .filter(email -> userRepository.existsByEmailAndDeletedIsFalse(email))
                        .collect(Collectors.toList());
                if(errorEmails != null && errorEmails.size() > 0){
                    errorData.add(ErrorIndexData.builder()
                            .index(invitation.getIndex())
                            .data(errorEmails)
                            .build());
                }
            }
        });
        if(errorData.size() > 0){
            return new ApiResponse(false, Const.ERROR_CODE.EMAIL_EXISTS, errorData);
        }
//        Map<String,Long> mapExist= new HashMap<>();
//        List<String> emails=userInviteList.stream().map(u->u.getEmail()).collect(Collectors.toList());
//        for(int i=0;i<emails.size();i++){
//            if(mapExist.containsKey(emails.get(i).toLowerCase())){
//                ApiResponse apiResponse = ApiResponse.error("Email duplicated in the request list.");
//                apiResponse.setData(Arrays.asList(emails.get(i)));
//                return ResponseEntity.badRequest().body(apiResponse);
//            }else
//                mapExist.put(emails.get(i).toLowerCase(),1L);
//        }
        return null;
    }
    @Transactional
    public List<UserInvite> inviteUser(List<UserInvite> userInviteList){
        User currUser= userService.getUserById(SecurityUtils.getUserId());
        userInviteList.stream().forEach(u-> {
            u.setSender(currUser);
            Company company = companyService.findById(u.getCompanyId());
            u.setCompany(company);
            Random random = new Random();
            Integer hashCode = random.nextInt(Integer.MAX_VALUE);
            while(userInviteRepository.existsByHashCode(hashCode)){
                hashCode = random.nextInt(Integer.MAX_VALUE);
            }
            u.setHashCode(hashCode);
            u.setEnabled(true);
        });
        List<UserInvite> list= userInviteRepository.saveAll(userInviteList);
        //to do send request
        sendMail(list);
        return list;
    }
    public void sendMail(List<UserInvite> userInviteList) {
        try {
            String supportCenter =  host+"/support/customer-support/";

            for (UserInvite ui:userInviteList) {
                List<String> receivers = Arrays.asList(ui.getEmail());
                String title = ui.getSender().getName() + " from " + ui.getSender().getCompany().getName() + " has invited you to join eMoldino platform";
                String setUpAccount = host + "/create-account/" + ui.getHashCode();
                String content = inviteUserEmailContent.generateMailContent(new Object[]{
                        ui.getSender().getName(),
                        ui.getSender().getEmail(),
                        ui.getSender().getCompany().getName(),
                        setUpAccount,
                        supportCenter
                });
                mailService.sendMailByContent(receivers, title, content);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ApiResponse getInvitation(Integer hashCode){
        UserInvite invitation = userInviteRepository.findByHashCode(hashCode);
        if(invitation == null || invitation.getJoined().booleanValue() == true || invitation.getEnabled() == false){
            return ApiResponse.error(Const.ERROR_CODE.INVALID_INVITATION);
        }
        ApiResponse response = ApiResponse.success();
        response.setData(invitation);
        return response;
    }

    @Transactional
    public ApiResponse createUser(UserPayload payload){
        if(payload.getHashCode() == null){
            return ApiResponse.error(Const.ERROR_CODE.INVALID_INVITATION);
        }
        UserInvite invitation = userInviteRepository.findByHashCode(payload.getHashCode());
        if(invitation == null || invitation.getJoined().booleanValue() == true || invitation.getEnabled() == false){
            return ApiResponse.error(Const.ERROR_CODE.INVALID_INVITATION);
        }
        User user = payload.getModel();

        boolean exists = userService.existByLoginIdOrEmail(user.getLoginId(), user.getEmail());

        if (exists) {
            return new ApiResponse(false, Const.ERROR_CODE.EMAIL_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
		SecurityUtils.resetPwdExpiredAt(user);
        user.setRequested(true);
        User finalUser = userService.save(user);
        versioningService.writeHistory(finalUser);
        userService.updateUserAlert(finalUser, payload.getAlertStatus());

        invitation.setJoined(true);
        invitation.setJoinedAt(Instant.now());
        userInviteRepository.save(invitation);

        List<UserInvite> invitations = userInviteRepository.findByEmail(invitation.getEmail());
        invitations.forEach(inv -> inv.setEnabled(false));
        userInviteRepository.saveAll(invitations);

        notificationService.createInviteUserNotification(invitation);

        return new ApiResponse(true, Const.SUCCESS, finalUser);
    }
}
