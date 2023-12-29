package vn.com.twendie.avis.security.core.service;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.UnauthorizedException;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.jdbc.repository.MemberCustomerAuthenRepo;
import vn.com.twendie.avis.security.jdbc.repository.UserRepository;

import javax.transaction.Transactional;

@Service("avisUserDetailService")
@Transactional
@Primary
public class AvisUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    private final MemberCustomerAuthenRepo memberCustomerRepo;

    public AvisUserDetailService(UserRepository userRepository, MemberCustomerAuthenRepo memberCustomerRepo) {
        this.userRepository = userRepository;
        this.memberCustomerRepo = memberCustomerRepo;
    }

    @Override
    public UserPrincipal loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(usernameOrEmail)
                .orElseThrow(() -> new UnauthorizedException("Not found user with username: " + usernameOrEmail)
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .displayMessage(Translator.toLocale("auth.wrong_credentials")));

        if (!user.getActive()) {
            throw new UnauthorizedException("User is inactive!!!")
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .displayMessage(Translator.toLocale("auth.user_is_inactive"));
        }

        if(user.getMemberCustomer() == null){
            user.setMemberCustomer(memberCustomerRepo.findFirstByUser(user));
        }

        return UserPrincipal.create(user);
    }

}
