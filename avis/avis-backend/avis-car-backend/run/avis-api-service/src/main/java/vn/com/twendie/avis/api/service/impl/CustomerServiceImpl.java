package vn.com.twendie.avis.api.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.adapter.ContractNotiInfoAdapter;
import vn.com.twendie.avis.api.adapter.CustomerAdminMemberUserAdapter;
import vn.com.twendie.avis.api.adapter.CustomerUserAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ValidUtils;
import vn.com.twendie.avis.api.core.validation.PasswordValidator;
import vn.com.twendie.avis.api.model.payload.*;
import vn.com.twendie.avis.api.model.projection.ContractInfoForNotiProjection;
import vn.com.twendie.avis.api.model.response.CustomerDTO;
import vn.com.twendie.avis.api.repository.CustomerRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.CustomerAlreadyExistException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.util.PasswordUtil;
import vn.com.twendie.avis.api.core.util.ValidObjectUtil;
import vn.com.twendie.avis.data.enumtype.CustomerTypeEnum;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.notification.service.NotificationContentService;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.NotificationSettingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "Customer")
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepo;
    private final ModelMapper modelMapper;
    private final CustomerTypeService customerTypeService;
    private final MemberCustomerService memberCustomerService;
    private final ContractService contractService;
    private final NotificationService notificationService;
    private final NotificationContentService notificationContentService;
    private final UserRoleService userRoleService;
    private final UserGroupService userGroupService;
    private final UserService userService;
    private final PasswordUtil passwordUtil;
    private final NotificationSettingService notificationSettingService;

    private final CustomerUserAdapter customerUserAdapter;
    private final CustomerAdminMemberUserAdapter customerAdminMemberUserAdapter;

    @Autowired
    public CustomerServiceImpl(CustomerRepo customerRepo,
                               ModelMapper modelMapper,
                               CustomerTypeService customerTypeService,
                               @Lazy MemberCustomerService memberCustomerService,
                               ContractService contractService,
                               NotificationService notificationService,
                               NotificationContentService notificationContentService,
                               UserRoleService userRoleService,
                               UserGroupService userGroupService,
                               UserService userService,
                               PasswordUtil passwordUtil,
                               CustomerUserAdapter customerUserAdapter,
                               CustomerAdminMemberUserAdapter customerAdminMemberUserAdapter,
                               NotificationSettingService notificationSettingService) {
        this.customerRepo = customerRepo;
        this.modelMapper = modelMapper;
        this.customerTypeService = customerTypeService;
        this.memberCustomerService = memberCustomerService;
        this.contractService = contractService;
        this.notificationService = notificationService;
        this.notificationContentService = notificationContentService;
        this.userRoleService = userRoleService;
        this.userGroupService = userGroupService;
        this.userService = userService;
        this.passwordUtil = passwordUtil;
        this.customerUserAdapter = customerUserAdapter;
        this.customerAdminMemberUserAdapter = customerAdminMemberUserAdapter;
        this.notificationSettingService = notificationSettingService;
    }

    @Override
    public CustomerDTO createOrUpdateEnterpriseCustomer(EnterpriseCustomerPayload payload,
                                                        User currentUser,
                                                        String customerCode,
                                                        List<String> userMemberCodes) {
        List<Notification> notifications = new ArrayList<>();
        List<Long> updatedMemberIds = new ArrayList<>();

        Customer customer = createNewEnterpriseCustomerIfNotExist(payload.getCustomerPayload(), currentUser, notifications, customerCode);
        MemberCustomer admin = createNewAdminIfNotExist(payload.getAdminPayload(), customer, updatedMemberIds);
        int index = 0;
        if (!CollectionUtils.isEmpty(payload.getUserPayloads())) {
            for (MemberCustomerPayload memberPayload : payload.getUserPayloads()) {
                ValidObjectUtil.trimReflectAndNormalizeString(memberPayload);
                if (memberPayload.isNew()) {
                    userService.checkUserExistsByUsername(memberPayload.getUsername());
                    createNewUser(memberPayload, customer, admin, userMemberCodes.get(index));
                    index++;
                } else {
                    updateUser(memberPayload, customer, admin, updatedMemberIds);
                }
            }
        }
        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
        customerDTO.setPosition(admin.getDepartment());
        customerDTO.setMobile(admin.getMobile());
        customerDTO.setCountryCode(admin.getCountryCode());
        customerDTO.setAdminId(admin.getId());
        customerDTO.setAdminName(admin.getName());
        customerDTO.setEmail(admin.getEmail());

        if (!CollectionUtils.isEmpty(updatedMemberIds)) {
            List<ContractInfoForNotiProjection> infos = contractService.findNeededInfoForNotiByListMemberId(updatedMemberIds);
            List<Notification> notifications1 = Lists.transform(infos, new ContractNotiInfoAdapter(
                    notificationContentService.findById(NotificationContentEnum.CONTRACT_CHANGE.getId())));

            List<Long> ids = notifications.stream().map(Notification::getUserId)
                    .collect(Collectors.toList());

            notifications1.forEach(notification -> {
                if (!ids.contains(notification.getUserId())) {
                    notifications.add(notification);
                }
            });

        }

        notifications.forEach(x -> notificationService.saveAndPush(x, true));

        return customerDTO;
    }

    private void createNewUser(MemberCustomerPayload newUser, Customer customer, MemberCustomer admin, String code) {
        MemberCustomer m = modelMapper.map(newUser, MemberCustomer.class);
        m.setId(null);
        m.setCustomer(customer);
        m.setParent(admin);
        m.setRole(MemberCustomerRoleEnum.USER.getCode());
        m.setCode(newUser.getUsername());
        m.setActive(true);

//        create user signature
        User user = new User();
        user.setUserRole(userRoleService.findById(UserRoleEnum.SIGNATURE.getId()));
        user.setCode(newUser.getUsername());
        user.setName(m.getName());
        user.setUsername(newUser.getUsername());
        user.setPassword(passwordUtil.getSignatureDefaultPassword());
        user.setActive(true);
        user.setEmail(newUser.getEmail());
        user.setMobile(newUser.getMobile());
        user.setCountryCode(newUser.getCountryCode());
        user.setMobileFull(newUser.getMobileFull());
        userService.save(user);

//        cretate notification setting
        NotificationSetting notificationSetting = new NotificationSetting();
        notificationSetting.setDay(false);
        notificationSetting.setWeek(false);
        notificationSetting.setMonth(false);
        notificationSetting.setUser(user);

        notificationSettingService.save(notificationSetting);

        m.setUser(user);

        MemberCustomer savedMember = memberCustomerService.save(m);
        checkMemberExistByEmailNotAdmin(newUser.getEmail(), savedMember);
        checkMemberExistByPhoneNumberNotAdmin(newUser.getMobile(), newUser.getCountryCode(), savedMember);
        checkMemberCustomerExistByCode(savedMember.getCode(), savedMember.getId());
    }

    private MemberCustomer createNewAdminIfNotExist(AdminCustomerPayload adminPayload, Customer customer,
                                                    List<Long> updatedMemberIds) {
        ValidObjectUtil.trimReflectAndNormalizeString(adminPayload);
        if (adminPayload.isNew()) {
            MemberCustomer newAdminMember = modelMapper.map(adminPayload, MemberCustomer.class);
            newAdminMember.setId(null);
            newAdminMember.setCustomer(customer);
            newAdminMember.setRole(MemberCustomerRoleEnum.ADMIN.getCode());
            newAdminMember.setActive(true);
            MemberCustomer saveAdminMember = memberCustomerService.save(newAdminMember);
            checkMemberExistByPhoneNumber(adminPayload.getMobile(), adminPayload.getCountryCode(), saveAdminMember);
            checkMemberCustomerExistByCode(saveAdminMember.getCode(), saveAdminMember.getId());

            // create new user
            User user = customerAdminMemberUserAdapter.apply(saveAdminMember, null);
            user.setPassword(passwordUtil.getCustomerDefaultPass());
            User createdUser = userService.save(user);
            userService.checkUserExistByUsernameAndIdNot(user.getUsername(), createdUser.getId());
            saveAdminMember.setUser(createdUser);
            return saveAdminMember;
        } else {
            return updateAdminMember(adminPayload, customer, updatedMemberIds);
        }
    }

    private Customer createNewEnterpriseCustomerIfNotExist(CustomerPayload customerPayload,
                                                           User currentUser,
                                                           List<Notification> notifications,
                                                           String customerCode) {
        ValidObjectUtil.trimReflectAndNormalizeString(customerPayload);
        if (customerPayload.isNew()) {
            customerPayload.setName(customerPayload.getName().toUpperCase());
            Customer newCustomer = modelMapper.map(customerPayload, Customer.class);
            newCustomer.setId(null);
            newCustomer.setMobile(null);
            Customer savedCustomer = customerRepo.save(newCustomer);

            checkExistByName(customerPayload.getName(), savedCustomer.getId());
            checkExistByTaxCode(customerPayload.getTaxCode(), savedCustomer.getId());

            savedCustomer.setCode(customerCode);
            savedCustomer.setCreatedBy(currentUser);
            savedCustomer.setCustomerType(customerTypeService.findById(CustomerTypeEnum.ENTERPRISE.value()));
            savedCustomer.setActive(true);
            return savedCustomer;
        } else {
            return updateCustomer(customerPayload, currentUser, notifications);
        }
    }

    public Customer findByIdAndDeletedFalse(Long id) {
        return customerRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Cannot find customer with id: " + id)
                        .displayMessage(Translator.toLocale("error.customer.not_found")));
    }

    private Customer updateCustomer(CustomerPayload customerPayload, User currentUser, List<Notification> list) {
        if (customerPayload.getId() == null) {
            throw new BadRequestException("Customer payload Id is null!!!");
        }

        checkExistByName(customerPayload.getName(), customerPayload.getId());
        checkExistByTaxCode(customerPayload.getTaxCode(), customerPayload.getId());

        Customer existCustomer = findByIdAndDeletedFalse(customerPayload.getId());
        avoidChangeCustomerCode(customerPayload.getCode(), existCustomer.getCode());

        if (!existCustomer.getCustomerType().getId().equals(CustomerTypeEnum.ENTERPRISE.value())) {
            throw new BadRequestException("Wrong customer type update!!!");
        }

        if (!customerPayload.getAddress().equals(existCustomer.getAddress())) {
            List<ContractInfoForNotiProjection> infos =
                    contractService.findNeededInfoForNotiByCustomerId(existCustomer.getId());
            List<Notification> notifications = Lists.transform(infos,
                    new ContractNotiInfoAdapter(
                            notificationContentService.findById(NotificationContentEnum.CONTRACT_CHANGE.getId())));
            list.addAll(notifications);
        }

        customerPayload.setName(customerPayload.getName().toUpperCase());
        modelMapper.map(customerPayload, existCustomer);
        existCustomer.setUpdatedBy(currentUser);

        return customerRepo.save(existCustomer);
    }

    private MemberCustomer updateAdminMember(AdminCustomerPayload adminPayload, Customer customer,
                                             List<Long> updatedMemberIds) {
        if (adminPayload.getId() == null) {
            throw new BadRequestException("Admin id is null!!!");
        }

        MemberCustomer existMember = memberCustomerService.findByIdAndCustomerId(adminPayload.getId(), customer.getId())
                .orElseThrow(() -> new NotFoundException("Cannot find admin with id: " + adminPayload.getId()
                        + " and customer id: " + customer.getId())
                        .displayMessage(Translator.toLocale("error.customer_member.not_found")
                                + " " + adminPayload.getName()));
        if(StringUtils.isNotEmpty(adminPayload.getMobile()) && !adminPayload.getMobile().equals(existMember.getMobile()))
        checkMemberExistByPhoneNumber(adminPayload.getMobile(), adminPayload.getCountryCode(),
                existMember);
        avoidChangeCustomerCode(adminPayload.getCode(), existMember.getCode());

        if (!adminPayload.getName().equals(existMember.getName())
                || !adminPayload.getMobile().equals(existMember.getMobile())) {
            updatedMemberIds.add(existMember.getId());
        }

        if (!adminPayload.getActive().equals(existMember.isActive())) {
            if (adminPayload.getActive()) {
                existMember.getCustomer().setActive(true);

                if (!CollectionUtils.isEmpty(existMember.getChildren())) {
                    existMember.getChildren().forEach(c -> c.setActive(true));
                }

            } else {
                checkAndUpdateUserMemberAndCustomerInactive(existMember);
            }
        }

        modelMapper.map(adminPayload, existMember);

        if (Objects.isNull(existMember.getUser())) {
            throw new NotFoundException("Cannot find customer user of customer with id: " + existMember.getId());
        }

        // update user info as admin info change
        User user = customerAdminMemberUserAdapter.apply(existMember, existMember.getUser());
//        userService.checkUserExistByUsernameAndIdNot(user.getUsername(), user.getId());

        if (Objects.nonNull(adminPayload.getPassword())) {
            user.setPassword(passwordUtil.encryptPassword(adminPayload.getPassword()));
            user.setLoginFailedTimes(0);
            user.setLoginTimes(0);
        }
        userService.save(user);

        return memberCustomerService.save(existMember);
    }

    private void checkAndUpdateUserMemberAndCustomerInactive(MemberCustomer existMember) {
        if (!CollectionUtils.isEmpty(existMember.getChildren())) {
            existMember.getChildren().forEach(c -> c.setActive(false));
        }

        if (memberCustomerService.isLastActiveAdmin(existMember)) {
            existMember.getCustomer().setActive(false);
        }
    }

    private void updateUser(MemberCustomerPayload memberPayload, Customer customer, MemberCustomer admin, List<Long> updatedMemberIds) {
        if (memberPayload.getId() == null) {
            throw new BadRequestException("User id is null!!!");
        }

        MemberCustomer existMember = memberCustomerService.findByIdAndCustomerId(memberPayload.getId(), customer.getId())
                .orElseThrow(() -> new NotFoundException("Cannot find user with id: " + memberPayload.getId()
                        + " and customer id: " + customer.getId())
                        .displayMessage(Translator.toLocale("error.customer_member.not_found")
                                + " " + memberPayload.getName()));
        if(StringUtils.isNotEmpty(memberPayload.getEmail()) && !memberPayload.getEmail().equals(existMember.getEmail())) {
            checkMemberExistByEmailNotAdmin(memberPayload.getEmail(), existMember);
        }

        if(StringUtils.isNotEmpty(memberPayload.getMobileFull()) && !memberPayload.getMobileFull().equals(existMember.getMobileFull())) {
            checkMemberExistByPhoneNumberNotAdmin(memberPayload.getMobile(), memberPayload.getCountryCode(),
                    existMember);
        }
        avoidChangeCustomerCode(memberPayload.getCode(), existMember.getCode());

        if (!memberPayload.getName().equals(existMember.getName())
                || !memberPayload.getMobile().equals(existMember.getMobile())) {
            updatedMemberIds.add(existMember.getId());
        }

        if (MemberCustomerRoleEnum.IGNORE.getCode().equals(existMember.getRole())) {
            User user = new User();
            user.setUserRole(userRoleService.findById(UserRoleEnum.SIGNATURE.getId()));
            user.setActive(true);
            user.setCode(existMember.getCode());
            user.setUsername(existMember.getCode());
            user.setPassword(passwordUtil.getSignatureDefaultPassword());
            user.setName(existMember.getName());
            user.setEmail(existMember.getEmail());
            user.setMobile(existMember.getMobile());
            user.setCountryCode(existMember.getCountryCode());
            user.setMobileFull(existMember.getMobileFull());
            userService.save(user);

            existMember.setUser(user);
            existMember.setRole(MemberCustomerRoleEnum.USER.getCode());
        } else if (StringUtils.isNotEmpty(memberPayload.getPassword()) && existMember.getUser() != null) {
            User user = existMember.getUser();
            user.setPassword(passwordUtil.encryptPassword(memberPayload.getPassword()));
            user.setLoginTimes(0);
            user.setLoginFailedTimes(0);
            userService.save(user);
        }

        if(existMember.getUser() != null){
            existMember.getUser().setEmail(memberPayload.getEmail());
            existMember.getUser().setCountryCode(memberPayload.getCountryCode());
            existMember.getUser().setMobile(memberPayload.getMobile());
            existMember.setName(memberPayload.getName());
            userService.save(existMember.getUser());
        }


        modelMapper.map(memberPayload, existMember);

        if (StringUtils.isBlank(existMember.getRole())) {
            existMember.setRole(MemberCustomerRoleEnum.USER.getCode());
        }

        memberCustomerService.save(existMember);
    }

    @Override
    public Customer createIndividualCustomer(IndividualCustomerPayload payload, User currentUser) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        checkBankNumberValid(payload.getBankAccountNumber());

        payload.setName(payload.getName().toUpperCase());
        Customer newIndividualCustomer = modelMapper.map(payload, Customer.class);

        setDefaultInfoForNewCustomer(newIndividualCustomer, currentUser);
        Customer customer = save(newIndividualCustomer);
        customer.setActive(true);

        checkCustomerExistByPhoneNumber(payload.getMobile(), payload.getCountryCode(), customer.getId());
        checkExistByIdCardAndCardType(payload.getIdCard(), payload.getCardType(), customer.getId());
        checkCustomerExistByCode(newIndividualCustomer.getCode(), customer.getId());

        User user = new CustomerUserAdapter(userRoleService, userGroupService)
                .apply(customer, null);
        user.setPassword(passwordUtil.getCustomerDefaultPass());

        User newUser = userService.save(user);
        userService.checkUserExistByUsernameAndIdNot(newUser.getUsername(), newUser.getId());
        customer.setUser(newUser);
        return customer;
    }

    private void setDefaultInfoForNewCustomer(Customer newIndividualCustomer, User currentUser) {
        newIndividualCustomer.setId(null);
        newIndividualCustomer.setTaxCode(null);
        newIndividualCustomer.setCreatedBy(currentUser);
        newIndividualCustomer.setCustomerType(customerTypeService.findById(CustomerTypeEnum.INDIVIDUAL.value()));
        newIndividualCustomer.setActive(true);
    }

    private void checkBankNumberValid(String bankAccountNumber) {
        if (!StringUtils.isBlank(bankAccountNumber)
                && !StringUtils.isNumeric(bankAccountNumber)) {
            throw new BadRequestException("Bank number wrong format")
                    .displayMessage(Translator.toLocale("customer.valid_error.bank_account_number_wrong_format"));
        }
    }

    private void checkCustomerExistByPhoneNumber(String phoneNumber, String countryCode, Long customerId) {
        if (customerRepo.existsByMobileAndCountryCodeAndIdNotAndDeletedFalse(phoneNumber, countryCode, customerId) ||
                memberCustomerService.existByPhoneNumberAndCountryCode(phoneNumber, countryCode)) {
            throw new CustomerAlreadyExistException("Customer phone number already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("error.customer.phone_number_already_exist"));
        }
    }

    private void checkMemberExistByPhoneNumber(String phoneNumber, String countryCode, MemberCustomer memberCustomer) {
        if (customerRepo.existsByMobileAndCountryCodeAndDeletedFalse(phoneNumber, countryCode) ||
                memberCustomerService.existByPhoneNumberAndCountryCodeAndIdNot(phoneNumber, countryCode, memberCustomer.getId())) {
            throw new CustomerAlreadyExistException("Customer phone number already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(String.format(Translator.toLocale("member_customer.error.phone_number_already_exist"), memberCustomer.getName()));
        }
    }

    private void checkMemberExistByPhoneNumberNotAdmin(String phoneNumber, String countryCode, MemberCustomer memberCustomer) {
//
//        UserRole userRole = userRoleService.findById(UserRoleEnum.ADMIN.getId());
        if (customerRepo.existsByMobileAndCountryCodeAndDeletedFalseAndUserIdNot(phoneNumber, countryCode, UserRoleEnum.ADMIN.getId()) > 0 ||
                memberCustomerService.existByPhoneNumberAndCountryCodeAndIdNotAndParentIdNotNull(phoneNumber, countryCode, memberCustomer.getId())) {
            throw new CustomerAlreadyExistException("Customer phone number already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(String.format(Translator.toLocale("member_customer.error.phone_number_already_exist"), memberCustomer.getName()));
        }
    }

    private void checkMemberExistByEmailNotAdmin(String email, MemberCustomer memberCustomer) {
        if (customerRepo.existsByEmailAndDeletedFalseAndIdNot(email, memberCustomer.getId()) ||
                memberCustomerService.existByEmailAndIdNot(email, memberCustomer.getId())) {
            throw new CustomerAlreadyExistException("Customer email number already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(String.format(Translator.toLocale("member_customer.error.email_already_exist"), memberCustomer.getName()));
        }
    }

    private void checkExistByName(String name, Long id) {
        if (customerRepo.existsByNameIgnoreCaseAndIdNotAndCustomerTypeIdAndDeletedFalse(
                ValidUtils.normalizeString(name.toUpperCase()), id, CustomerTypeEnum.ENTERPRISE.value())) {
            throw new CustomerAlreadyExistException("Customer name already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("error.enterprise_customer.name_already_exist"));
        }
    }

    private void checkExistByTaxCode(String taxCode, Long id) {
        if (customerRepo.existsByTaxCodeAndIdNotAndDeletedFalse(taxCode, id)) {
            throw new CustomerAlreadyExistException("Customer tax code already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("error.enterprise_customer.tax_code_already_exist"));
        }
    }

    @Override
//    @Cacheable(key = "#id")
    public Customer findById(long id) {
        return customerRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found customer with id: " + id)
                        .code(HttpStatus.NOT_FOUND.value())
                        .displayMessage(Translator.toLocale("error.customer.not_found")));
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepo.save(customer);
    }

    @Override
    public Boolean updateIndividualCustomer(IndividualCustomerPayload payload, User currentUser) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        checkBankNumberValid(payload.getBankAccountNumber());
        checkCustomerExistByPhoneNumber(payload.getMobile(), payload.getCountryCode(), payload.getId());
        checkExistByIdCardAndCardType(payload.getIdCard(), payload.getCardType(), payload.getId());
        Customer existCustomer = findByIdAndDeletedFalse(payload.getId());
        avoidChangeCustomerCode(payload.getCode(), existCustomer.getCode());

        if (!existCustomer.getCustomerType().getId().equals(CustomerTypeEnum.INDIVIDUAL.value())) {
            throw new BadRequestException("Wrong customer type update!!!");
        }

        boolean isUpdate = false;

        payload.setName(payload.getName().toUpperCase());
        if (!payload.getName().equals(existCustomer.getName().toUpperCase())
                || !payload.getMobile().equals(existCustomer.getMobile())
                || !payload.getAddress().equals(existCustomer.getAddress())) {
            isUpdate = true;
        }

        modelMapper.map(payload, existCustomer);
        existCustomer.setUpdatedBy(currentUser);

        if (Objects.isNull(existCustomer.getUser())) {
            throw new NotFoundException("Cannot find customer user of customer with id: " + existCustomer.getId());
        }

        User user = customerUserAdapter.apply(existCustomer, existCustomer.getUser());
//        userService.checkUserExistByUsernameAndIdNot(user.getUsername(), user.getId());

        if (Objects.nonNull(payload.getPassword())) {
            user.setPassword(passwordUtil.encryptPassword(payload.getPassword()));
            user.setLoginFailedTimes(0);
            user.setLoginTimes(0);
        }

        userService.save(user);

        if (isUpdate) {
            List<ContractInfoForNotiProjection> infos =
                    contractService.findNeededInfoForNotiByCustomerId(existCustomer.getId());
            List<Notification> notifications = Lists.transform(infos,
                    new ContractNotiInfoAdapter(
                            notificationContentService.findById(NotificationContentEnum.CONTRACT_CHANGE.getId())));
            notifications.forEach(x -> notificationService.saveAndPush(x, true));
        }

        return true;
    }

    private void avoidChangeCustomerCode(String payloadCode, String customerCode) {
        if (!payloadCode.equals(customerCode)) {
            throw new BadRequestException("Cannot change customer code!!!")
                    .displayMessage(Translator.toLocale("error.customer.code_cannot_be_changed"));
        }
    }

    private void checkExistByIdCardAndCardType(String idCard, Integer cardType, Long id) {
        if (customerRepo.existsByIdCardAndCardTypeAndIdNotAndDeletedFalse(idCard, cardType, id)) {
            throw new CustomerAlreadyExistException("Customer id card already exist!!!")
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("error.customer.id_card_already_exist"));
        }
    }

    public void checkCustomerExistByCode(String code, Long id) {
        if (customerRepo.existsByCodeAndIdNot(code, id)) {
            throw new CustomerAlreadyExistException("Customer code already exist with code: " + code)
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("error.customer.code_already_exist"));
        }
    }

    public void checkMemberCustomerExistByCode(String code, Long id) {
        if (memberCustomerService.existsByCodeAndIdNot(code, id)) {
            throw new CustomerAlreadyExistException("Member code already exist with code: " + code)
                    .code(HttpStatus.CONFLICT.value())
                    .displayMessage(Translator.toLocale("member_customer.error.code_already_exist"));
        }
    }

    @Override
    public Boolean deleteIndividualCustomer(Long id) {
        Customer customer = customerRepo.findByIdAndCustomerTypeId(id, CustomerTypeEnum.INDIVIDUAL.value())
                .orElseThrow(() -> new NotFoundException("Not found customer with id: " + id)
                        .code(HttpStatus.NOT_FOUND.value())
                        .displayMessage(Translator.toLocale("error.customer.not_found")));

        if (customer.isDeleted()) {
            throw new BadRequestException("Customer already deleted!!!")
                    .displayMessage(Translator.toLocale("error.customer.already_deleted"));
        }

        if (customer.isInContract()) {
            throw new BadRequestException("Customer still exist in contract!")
                    .displayMessage(Translator.toLocale("error.customer.cannot_delete_within_contract"));
        }

        if (CustomerTypeEnum.INDIVIDUAL.code().equals(customer.getCustomerType().getCode())) {
            customer.setDeleted(true);

            if (Objects.nonNull(customer.getUser())) {
                customer.getUser().setDeleted(true);
            }
            return true;
        } else {
            throw new BadRequestException("This is not individual customer!!!");
        }
    }

    @Override
    public void updateInContract(Customer customer) {
        if (!customer.isInContract()) {
            customer.setInContract(true);
            save(customer);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String suggestCustomerCode() {
        String prefixCode = AvisApiConstant.PrefixCode.CUSTOMER;
        String code = customerRepo.getNewestCodeByPrefix(prefixCode);

        if (Objects.isNull(code)) {
            return prefixCode + AvisApiConstant.DEFAULT_SUFFIX_CODE;
        } else {
            String suffixCode = code.split(prefixCode)[1];
            return String.format(prefixCode + "%04d", Integer.parseInt(suffixCode) + 1);
        }
    }

}
