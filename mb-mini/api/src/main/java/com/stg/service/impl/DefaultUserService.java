package com.stg.service.impl;

import com.stg.config.auth.PublicKeyProvider;
import com.stg.entity.credential.Credential;
import com.stg.entity.credential.EmailAndPasswordCredential;
import com.stg.entity.credential.EmailAndPasswordCredentialReset;
import com.stg.entity.credential.EmailAndPasswordCredentialResetStatus;
import com.stg.entity.user.BlackListToken;
import com.stg.entity.user.Feature;
import com.stg.entity.user.FeatureUser;
import com.stg.entity.user.User;
import com.stg.errors.*;
import com.stg.repository.*;
import com.stg.service.MailService;
import com.stg.service.UserService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.credential.CredentialDto;
import com.stg.service.dto.credential.EmailAndPasswordCredentialDto;
import com.stg.service.dto.credential.PasswordGenerationResponseDto;
import com.stg.service.dto.email.ForgotPasswordDto;
import com.stg.service.dto.email.InputEmailDto;
import com.stg.service.dto.user.*;
import com.stg.service.impl.password.*;
import com.stg.service.impl.visitor.credential.ToEntityCredentialDtoVisitor;
import com.stg.service.impl.visitor.user.ToDtoUserVisitor;
import com.stg.service.impl.visitor.user.ToEntityUserDtoVisitor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.passay.CharacterRule;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.utils.Common.createCharacterRules;
import static com.stg.utils.EmailType.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final ToEntityUserDtoVisitor toEntityUserDtoVisitor;
    private final PasswordEncoder passwordEncoder;
    private final FeatureUserRepository featureUserRepository;
    private final MailService mailService;
    private final PublicKeyProvider publicKeyProvider;
    private final BlackListTokenRepository blackListTokenRepository;
    private final PasswordValidator passwordValidator;
    private final FeatureRepository featureRepository;
    private final CredentialRepository credentialRepository;
    private final EmailAndPasswordCredentialResetRepository credentialResetRepository;


    @Autowired
    UserDetailsServiceImpl userServiceDetail;

    @Autowired
    @Lazy
    private DefaultUserService self;

    @Override
    public UserDto createUser(Long userId, UserDto userDto) {
        isSuperAdmin(userId);
        try {
            String newPassword = generateRandomPassword(userDto.getEmail()).getNewPassword();
            Set<CredentialDto> credentialDtos = new HashSet<>();
            EmailAndPasswordCredentialDto usernameAndPasswordCredentialDto = new EmailAndPasswordCredentialDto();
            usernameAndPasswordCredentialDto.setEmail(userDto.getEmail());
            usernameAndPasswordCredentialDto.setPassword(newPassword);
            credentialDtos.add(usernameAndPasswordCredentialDto);
            userDto.setCredentials(credentialDtos);
            User user = userDto.accept(toEntityUserDtoVisitor);
            User savedUser = self.saveUserInNewTransaction(user);
            FeatureUser featureUser = new FeatureUser();
            featureUser.setUser(savedUser);
            featureUser.setFeature("[]");
            if (userDto.getRole().equals(User.Role.ADMIN.toString())) {
                List<Feature> features = getEnabledFeature();
                List<String> featureName = features
                        .stream()
                        .map(Feature::getName)
                        .collect(Collectors.toList());
                featureUser.setFeature(featureName.toString());
            }
            featureUserRepository.save(featureUser);
            InputEmailDto emailDataDto = generateData(user, newPassword);
            mailService.sendMail(GENERATE_ACCOUNT_PASSWORD, emailDataDto);
            return savedUser.accept(new ToDtoUserVisitor(false, featureUserRepository));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Email " + userDto.getEmail() + " đã tồn tại. Vui lòng kiểm tra lại");
        }
    }

    @Override
    public UserDto editUser(Long superUserId, UserEditingDto editingDto, Long userId) {
        isSuperAdmin(superUserId);
        User user = findUser(userId);
        EmailAndPasswordCredential credential = getEmailAndPasswordCredential(user);

        String newEmail = editingDto.getEmail();
        if (StringUtils.isNotBlank(newEmail)) {
            newEmail = newEmail.toLowerCase();
            if (StringUtils.isNotEmpty(user.getEmail()) &&
                    !newEmail.equals(user.getEmail().toLowerCase())) {
                user.setEmail(newEmail);
                credential.setEmail(newEmail);
                log.info("Updating email={} for user with id={}", newEmail, user.getId());
            }
        }

        String newFirstName = editingDto.getFirstName();
        if (StringUtils.isNotEmpty(newFirstName)) {
            user.setFirstName(newFirstName);
        }
        String newLastName = editingDto.getLastName();
        if (StringUtils.isNotEmpty(newLastName)) {
            user.setLastName(newLastName);
        }
        user.setFullName(user.getFirstName() + " " + user.getLastName());
        user.setEnabled(editingDto.getEnabled());
        try {
            User savedUser = self.saveUserInNewTransaction(user);
            return savedUser.accept(new ToDtoUserVisitor(false, featureUserRepository));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Email " + editingDto.getEmail() + " đã tồn tại. Vui lòng kiểm tra lại");
        }
    }

    @Override
    public void addCredential(Long userId, CredentialDto credentialDto) {
        User user = findApplicationUser(userId);
        Credential newCredential = credentialDto.accept(new ToEntityCredentialDtoVisitor(passwordEncoder));
        user.addCredential(newCredential);
    }

    @Override
    public PasswordGenerationResponseDto generateRandomPassword(String email) {
        PasswordGenerator passwordGenerator = new PasswordGenerator();

        ValidationRules validationRules = PasswordRequirements.DEFAULT;

        List<CharacterRule> characterRules = createCharacterRules(validationRules);

        String password = passwordGenerator.generatePassword(validationRules.getMinimumLength(), characterRules);

        log.info("Generated random password for email {} with password: {}", email, password);

        return new PasswordGenerationResponseDto(password);
    }

    @Override
    public UserDto detailMe(Long userId) {
        User user = findUser(userId);
        return user.accept(new ToDtoUserVisitor(false, featureUserRepository));
    }

    @Override
    public List<UserDto> listUsersScroll(int count, Long lastId) {
        Pageable pageable = PageRequest.of(0, count);
        List<User> users = userRepository.listUsers(lastId, pageable);
        return users.stream().map(user -> user.accept(new ToDtoUserVisitor(false, featureUserRepository)))
                .collect(Collectors.toList());
    }

    @Override
    public PaginationResponse<UserDto> listUsers(Long userId, int offset, int size, String query) {
        isSuperAdmin(userId);
        PaginationResponse<UserDto> response = new PaginationResponse<>();
        List<User> users = userRepository.listUsers(offset, size, query);
        response.setData(users.stream().map(user -> user.accept(new ToDtoUserVisitor(false, featureUserRepository)))
                .collect(Collectors.toList()));
        long totalUser = userRepository.totalUsers(query);
        response.setTotalItem(totalUser);
        response.setPageSize(size);
        return response;
    }

    @Override
    public void updateFeatureUsers(Long userId, UserFeatureDto userFeatureDto) {
        isSuperAdmin(userId);
        List<Long> superAdminIds = userRepository.listSuperUsers().stream().map(User::getId).collect(Collectors.toList());
        List<Feature> featureByIds = getFeatureByIds(userFeatureDto.getFeatureIds());
        List<String> featureName = featureByIds
                .stream()
                .map(Feature::getName)
                .collect(Collectors.toList());
        featureUserRepository.updateByUserIds(superAdminIds, featureName.toString());
        featureRepository.disabledAll();
        featureRepository.enabled(featureByIds.stream().map(Feature::getId).collect(Collectors.toList()));
    }

    @Override
    public void forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        List<User> usersByEmailIn = userRepository.findUsersByEmailIn(List.of(forgotPasswordDto.getEmailTo()));
        if (!usersByEmailIn.isEmpty()) {
            User user = usersByEmailIn.get(0);

            if (!user.getEnabled()) {
                throw new UserInactiveException("Tài khoản của bạn chưa được kích hoạt. Vui lòng liên hệ với Super Admin để kích hoạt tài khoản");
            }

            EmailAndPasswordCredential emailAndPasswordCredential = getEmailAndPasswordCredential(user);
            String newPass = generateRandomPassword(forgotPasswordDto.getEmailTo()).getNewPassword();
            emailAndPasswordCredential.setPassword(passwordEncoder.encode(newPass));

            InputEmailDto emailDataDto = generateData(user, newPass);

            mailService.sendMail(FORGOT_PASSWORD, emailDataDto);
        } else {
            throw new UserNotFoundException("Email không tồn tại");
        }
    }

    @Override
    public void revoke(String token, Long userId) {
        Claims claims = getClaimsFromToken(token);
        String jti = (String) claims.get("jti");
        BlackListToken blackListToken = new BlackListToken();
        blackListToken.setUserId(userId);
        blackListToken.setJti(jti);
        blackListTokenRepository.save(blackListToken);
        log.info("User with ID {} has been logout.", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePassDTO changePassDTO) {
        User user = findApplicationUser(userId);
        EmailAndPasswordCredential emailAndPasswordCredential = getEmailAndPasswordCredential(user);
        if (!passwordEncoder.matches(changePassDTO.getOldPassword(), emailAndPasswordCredential.getPassword())) {
            throw new CredentialNotMatchingException("Mật khẩu hiện tại không chính xác. Vui lòng nhập lại");
        }
        ValidationInput input = createValidationInput(emailAndPasswordCredential, changePassDTO.getNewPassword());
        validatePassword(input, PasswordRequirements.DEFAULT);

        emailAndPasswordCredential.setPassword(passwordEncoder.encode(changePassDTO.getNewPassword()));
    }

    @Override
    public List<FeatureDto> getAllFeatures(Long userId) {
        isSuperAdmin(userId);
        List<Feature> features = featureRepository.findAll();
        return features.stream()
                .sorted(Comparator.comparingInt(Feature::getId))
                .map(o -> new FeatureDto()
                        .setId(o.getId())
                        .setName(o.getName())
                        .setEnabled(o.getEnabled()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto detailUser(Long superAdminId, Long userId) {
        isSuperAdmin(superAdminId);
        User user = findUser(userId);
        return user.accept(new ToDtoUserVisitor(false, featureUserRepository));
    }

    @Override
    public void createEmailAndPasswordCredentialReset(ChangePassRequestDto changePassRequestDto) {
        String email = changePassRequestDto.getEmail();
        Optional<EmailAndPasswordCredential> credentialOptional =
                credentialRepository.findByEmailAndEnabledTrue(email);

        if (credentialOptional.isEmpty()) {
            log.info("Attempt to get 'forgot password' email for non-existent combination of email={} ", email);
            return;
        }

        EmailAndPasswordCredential credential = credentialOptional.get();
        EmailAndPasswordCredentialReset credentialReset = new EmailAndPasswordCredentialReset();
        credentialReset.setEmailAndPasswordCredential(credential);
        credentialReset.addStatus(
                new EmailAndPasswordCredentialResetStatus(EmailAndPasswordCredentialResetStatus.Status.CREATED));
        credentialReset = credentialResetRepository.save(credentialReset);

        log.info("Saved password reset request with ID={} for user with email={}",
                credentialReset.getId(), email);

        InputEmailDto emailDataDto = new InputEmailDto()
                .setEmailTo(email)
                .setFirstname(credential.getUser().getFirstName())
                .setLastname(credential.getUser().getLastName())
                .setToken(credentialReset.getVerificationToken());

        mailService.sendMail(RESET_PASSWORD, emailDataDto);
    }

    @Override
    public void confirmEmailAndPasswordCredentialReset(ChangePassConfirmDto passConfirmDto) {
        EmailAndPasswordCredentialReset requestedReset =
                getValidEmailAndPasswordCredentialReset(passConfirmDto);
        EmailAndPasswordCredential credential = requestedReset.getEmailAndPasswordCredential();
        User user = credential.getUser();
        Long userId = user.getId();

        log.info("Retrieved EmailAndPasswordCredentialReset with token={} for user with ID={} ",
                requestedReset.getVerificationToken(), userId);

        LocalDateTime now = LocalDateTime.now();
        List<EmailAndPasswordCredentialReset> resets = credentialResetRepository.
                findByEmailAndPasswordCredentialIdAndValidUntilAfterAndIdNot(credential.getId(), now, requestedReset.getId());

        log.info("Retrieved all other valid EmailAndPasswordCredentialResets for user with ID={} ",
                userId);

        for (EmailAndPasswordCredentialReset reset : resets) {
            if (reset.getLastStatus().getStatus() !=EmailAndPasswordCredentialResetStatus.Status.CREATED) {
                continue;
            }

            EmailAndPasswordCredentialResetStatus.Status expired =
                    EmailAndPasswordCredentialResetStatus.Status.EXPIRED;
            reset.addStatus(new EmailAndPasswordCredentialResetStatus(expired));
            reset.setValidUntil(now);

            log.info("Set UsernameAndPasswordCredentialReset with ID={} for user with ID={}" +
                            " to status={} and valid until=now",
                    reset.getId(), userId, expired.name());
        }

        requestedReset.setValidUntil(now);
        requestedReset.addStatus(
                new EmailAndPasswordCredentialResetStatus(EmailAndPasswordCredentialResetStatus.Status.VERIFIED));

        log.info("Verified application user password reset with ID={} ", requestedReset.getId());

        ValidationInput input = createValidationInput(credential, passConfirmDto.getNewPassword());
        validatePassword(input, PasswordRequirements.DEFAULT);

        credential.setPassword(passwordEncoder.encode(passConfirmDto.getNewPassword()));
        credential.setNonExpired(Boolean.TRUE);
        log.info("Changed password for user with id={} ",
                requestedReset.getEmailAndPasswordCredential().getId());
    }

    private EmailAndPasswordCredentialReset getValidEmailAndPasswordCredentialReset(
            ChangePassConfirmDto passConfirmDto) {
        String token = passConfirmDto.getVerificationToken();
        EmailAndPasswordCredentialReset credentialReset =
                credentialResetRepository.findByVerificationTokenAndValidUntilAfter(token, LocalDateTime.now());

        if (credentialReset == null) {
            throw new EmailAndPasswordCredentialResetNotFoundException(
                    "Could not find user password reset for token=" + token);
        } else if (credentialReset.getLastStatus().getStatus() !=
                EmailAndPasswordCredentialResetStatus.Status.CREATED) {
            throw new EmailAndPasswordCredentialResetInvalidException("User password reset with token=" +
                    token + " already used");
        }

        return credentialReset;
    }

    private List<Feature> getFeatureByIds(List<Integer> ids) {
        return featureRepository.findByIdIn(ids);
    }

    private List<Feature> getEnabledFeature() {
        return featureRepository.getAllByEnabled(true);
    }

    private static ValidationInput createValidationInput(EmailAndPasswordCredential credential, String newPass) {
        return new ValidationInput()
                .setEmail(credential.getEmail())
                .setPassword(newPass);
    }

    private InputEmailDto generateData(User user, String newPassword) {
        return new InputEmailDto()
                .setEmailTo(user.getEmail())
                .setFirstname(user.getFirstName())
                .setLastname(user.getLastName())
                .setNewPassword(newPassword);
    }

    private void validatePassword(ValidationInput input, ValidationRules rules) {
        ValidationResult result = passwordValidator.validate(input, rules);

        if (!result.isValid()) {
            throw new PasswordValidationFailedException(result
                    .getValidationMessages()
                    .stream()
                    .collect(Collectors.joining(",")));
        }
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(publicKeyProvider.getPublicKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong token");
        }
    }

    private EmailAndPasswordCredential getEmailAndPasswordCredential(User user) {
        return (EmailAndPasswordCredential) user.getCredentials().stream()
                .filter(EmailAndPasswordCredential.class::isInstance)
                .findAny()
                .orElseThrow(() -> new CredentialNotFoundException(
                        "Cannot find email and password credential for user with id=" + user.getId()));
    }

    private void isSuperAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    protected User saveUserInNewTransaction(User user) {
        return userRepository.save(user);
    }

    private User findUser(long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Has not user with id: " + userId));
    }

}
