package saleson.api.auth;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import saleson.api.user.UserRepository;
import saleson.api.user.payload.UserPayload;
import saleson.common.config.Const;
import saleson.common.security.OpUserDetailsService;
import saleson.common.security.UserPrincipal;
import saleson.common.util.SecurityUtils;
import saleson.model.AccessToken;
import saleson.model.User;
import saleson.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Autowired
    private OpUserDetailsService opUserDetailsService;

    @Value("${api.expired.time}")
    private Integer expiredTime;

    public ResponseEntity login(UserPayload payload) {
        User user = userRepository.findByEmailAndDeletedIsFalse(payload.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_NOT_EXIST);
        }
        if (!passwordEncoder.matches(payload.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Const.ERROR_CODE.INVALID_PASSWORD);
        }
        if (!user.isEnabled()) {
            return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_NOT_ACTIVE);
        }

/*
        String genToken = RandomStringUtils.randomAlphanumeric(64);
        AccessToken accessToken = new AccessToken(user.getId(), genToken);

        accessTokenRepository.save(accessToken);
*/
        String genToken = genToken(user.getId(),
                payload.getDeviceToken(), payload.getUdid(), payload.getDeviceType());
        UserDTO userDTO = new UserDTO(user, genToken);
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        setUserAuthentication(user);

        return ResponseEntity.ok(userDTO);
    }
    public String genToken(Long userId, String deviceToken, String udid,String deviceType ){
        String genToken = RandomStringUtils.randomAlphanumeric(64);
        AccessToken accessToken = new AccessToken(userId, genToken,
                deviceToken, udid, deviceType,expiredTime);

        accessTokenRepository.save(accessToken);
        return genToken;
    }

    public User getCurrentUser(HttpServletRequest httpServletRequest) {
        User user = null;
        String token = httpServletRequest.getHeader(Const.HEADER_TOKEN);

        if (!StringUtils.isEmpty(token)) {
            AccessToken accessToken = accessTokenRepository.findByAccessToken(token);
            Instant validDate=Instant.now().minus(expiredTime, ChronoUnit.MINUTES);

//            if (accessToken != null && !accessToken.isDeleted() && accessToken.getExpiredTime() != null && accessToken.getExpiredTime().isAfter(Instant.now())) {
            if (accessToken != null && !accessToken.isDeleted() && accessToken.getCreatedAt() != null && accessToken.getCreatedAt().isAfter(validDate)) {
                user = userRepository.findById(accessToken.getUserId()).orElse(null);
                if (user != null && !user.isEnabled()) return null;
            }
            setUserAuthentication(user);
        }else if(SecurityUtils.getUserId()!=null){
            user = userRepository.findById(SecurityUtils.getUserId()).orElse(null);
        }
        return user;
    }
    public void setUserAuthentication(User user){
        if (user!=null) {
                /*
                    Note that you could also encode the user's username and roles inside JWT claims
                    and create the UserDetails object by parsing those claims from the JWT.
                    That would avoid the following database hit. It's completely up to you.
                 */
            UserDetails userDetails = UserPrincipal.create(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
    public void clearContext(){
        SecurityContextHolder.clearContext();
    }

}
