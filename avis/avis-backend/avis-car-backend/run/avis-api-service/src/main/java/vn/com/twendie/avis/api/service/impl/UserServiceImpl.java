package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.adapter.CreateContractDriverDTOAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.PageUtils;
import vn.com.twendie.avis.api.core.util.ValidObjectUtil;
import vn.com.twendie.avis.api.core.util.ValidUtils;
import vn.com.twendie.avis.api.model.payload.*;
import vn.com.twendie.avis.api.model.projection.UserProjection;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.repository.UserRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.util.PasswordUtil;
import vn.com.twendie.avis.api.util.PhoneNumberUtil;
import vn.com.twendie.avis.api.validation.DriverValidation;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.data.model.Branch;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.api.constant.AvisApiConstant.DRIVER_STATUS_ENUMS;
import static vn.com.twendie.avis.data.enumtype.DepartmentEnum.*;
import static vn.com.twendie.avis.data.enumtype.DriverStatusEnum.*;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.DRIVER_ID;
import static vn.com.twendie.avis.data.enumtype.UserRoleEnum.DRIVER;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final ContractChangeHistoryService contractChangeHistoryService;
    private final UserGroupService userGroupService;
    private final BranchService branchService;
    private final UserDepartmentService userDepartmentService;
    private final UserRoleService userRoleService;

    private final ListUtils listUtils;
    private final PasswordUtil passwordUtil;

    private final ModelMapper modelMapper;
    private final CreateContractDriverDTOAdapter createContractDriverDTOAdapter;

    private final DriverValidation driverValidation;

    public UserServiceImpl(UserRepo userRepo,
                           @Lazy ContractChangeHistoryService contractChangeHistoryService,
                           UserGroupService userGroupService,
                           BranchService branchService,
                           UserDepartmentService userDepartmentService,
                           UserRoleService userRoleService,
                           ListUtils listUtils,
                           PasswordUtil passwordUtil,
                           ModelMapper modelMapper,
                           CreateContractDriverDTOAdapter createContractDriverDTOAdapter,
                           DriverValidation driverValidation) {
        this.userRepo = userRepo;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.userGroupService = userGroupService;
        this.branchService = branchService;
        this.userDepartmentService = userDepartmentService;
        this.userRoleService = userRoleService;
        this.listUtils = listUtils;
        this.passwordUtil = passwordUtil;
        this.modelMapper = modelMapper;
        this.createContractDriverDTOAdapter = createContractDriverDTOAdapter;
        this.driverValidation = driverValidation;
    }

    @Override
    public User findDriverByIdAndGroups(Long id, DriverGroupEnum... groups) {
        if (Objects.isNull(id)) {
            return null;
        }
        Set<Long> groupIds = Arrays.stream(groups)
                .map(DriverGroupEnum::getId)
                .collect(Collectors.toSet());
        return userRepo.findByIdAndUserRoleNameAndUserGroupIdInAndDeletedFalse(id, DRIVER.value(), groupIds)
                .orElseThrow(() -> new NotFoundException("Not found driver with id: " + id)
                        .displayMessage(Translator.toLocale("driver.not_found")));
    }

    @Override
    public Map<Long, UserProjection> findByIdIn(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        } else {
            return userRepo.findByIdIn(ids)
                    .stream()
                    .collect(Collectors.toMap(UserProjection::getId, v -> v));
        }
    }

    @Override
    public GeneralPageResponse<CreateContractDriverDTO> driverSuggestionsByName(DriverSuggestionPayload payload,
                                                                                boolean forContractWithDriver) {
        List<User> drivers;
        if (StringUtils.isBlank(payload.getDriverName())) {
            return GeneralPageResponse.toResponse(Page.empty());
        } else if (forContractWithDriver) {
            drivers = userRepo.findAllAvisDriverByName(
                    ValidUtils.normalizeString(payload.getDriverName().trim()),
                    DRIVER.getId(),
                    AvisApiConstant.AVIS_DRIVER_GROUP);
        } else {
            drivers = userRepo.findAllCustomerDriverByName(
                    ValidUtils.normalizeString(payload.getDriverName().trim()),
                    DRIVER.getId(),
                    DriverGroupEnum.CUSTOMER.getId());
        }

        if (CollectionUtils.isEmpty(drivers)) {
            return GeneralPageResponse.toResponse(Page.empty());
        }

        return GeneralPageResponse.toResponse(PageUtils
                .toPage(payload.getPage(), payload.getSize(), listUtils.transform(drivers, createContractDriverDTOAdapter)));
    }

    @Override
    public User findByIdIgnoreDelete(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found with id: " + id)
                        .displayMessage(Translator.toLocale("driver.not_found")));
    }

    @Override
    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public void assignToContract(User driver, Contract contract) {
        if (Objects.nonNull(driver)) {
            driver.setLendingContractId(driver.getCurrentContractId());
            driver.setCurrentContractId(contract.getId());
            driver.setInContract(true);
            updateDriverStatus(driver);
        }
    }

    @Override
    public void unAssignFromContract(User driver, Contract contract) {
        if (Objects.nonNull(driver)) {
            if (contract.getId().equals(driver.getCurrentContractId())) {
                driver.setCurrentContractId(driver.getLendingContractId());
            }
            driver.setLendingContractId(null);
            updateDriverStatus(driver);
        }
    }

    @Override
    public void updateDriverStatus(User driver) {
        if (Objects.nonNull(driver) && !UNAVAILABLE.getValue().equals(driver.getStatus())) {
            if (Objects.nonNull(driver.getCurrentJourneyDiaryId())) {
                driver.setStatus(UNAVAILABLE.getValue());
            } else if (Objects.isNull(driver.getCurrentContractId()) && Objects.isNull(driver.getLendingContractId())) {
                driver.setStatus(WAITING.getValue());
            } else {
                driver.setStatus(APPOINTED.getValue());
            }
        }
    }

    @Override
    public User getContractDriverAtTime(Contract contract, Timestamp timestamp) {
        ContractChangeHistory history = contractChangeHistoryService
                .findLastChangeOfField(contract, DRIVER_ID.getName(), timestamp);
        if (Objects.nonNull(history)) {
            if (!timestamp.before(history.getFromDate())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getNewValue()));
            } else if (Objects.nonNull(history.getOldValue())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getOldValue()));
            } else {
                return null;
            }
        } else {
            return contract.getDriver();
        }
    }

    @Override
    public User createDriver(CreateDriverPayload createDriverPayload) {
        ValidObjectUtil.trimReflectAndNormalizeString(createDriverPayload);
        driverValidation.validDriverRequireInfo(createDriverPayload, createDriverPayload.getUserGroupId());

        User user = modelMapper.map(createDriverPayload, User.class);
        user.setUserRole(userRoleService.findById(DRIVER.getId()));
        user.setUsername(createDriverPayload.getCode());
        user.setPassword(passwordUtil.getDriverDefaultPass());
        user.setActive(true);
        user.setMobileFull(PhoneNumberUtil.toMobileFullPattern(createDriverPayload.getCountryCode(),
                createDriverPayload.getMobile()));
        setDriverRelationInfo(user, createDriverPayload.getBranchId(),
                createDriverPayload.getUserGroupId(), createDriverPayload.getUnitOperatorId());
        User savedUser = save(user);

        checkExistDriverInfo(savedUser);

        return savedUser;
    }

    private void checkExistDriverInfo(User savedUser) {
        checkUserExistByUsernameAndIdNot(savedUser.getUsername(), savedUser.getId());
        checkExistUserCode(savedUser.getCode(), savedUser.getId());

        if (!DriverGroupEnum.CUSTOMER.getId().equals(savedUser.getUserGroup().getId())) {
            checkDriverExistWithIdCardAndUserGroup(savedUser);
        } else {
            checkCustomerDriverExistByMobile(savedUser);
        }
    }

    private void checkCustomerDriverExistByMobile(User savedUser) {
        if (userRepo.existsByMobileAndCountryCodeAndUserGroupIdAndIdNotAndDeletedFalse(savedUser.getMobile(), savedUser.getCountryCode(), savedUser.getUserGroup().getId(), savedUser.getId())) {
            throw new BadRequestException("Driver phone number already exist!!!")
                    .displayMessage(Translator.toLocale("driver.valid_error.mobile_already_exist"));
        }
    }

    @Override
    public User updateDriver(EditDriverPayload editDriverPayload) {
        ValidObjectUtil.trimReflectAndNormalizeString(editDriverPayload);

        User user = findById(editDriverPayload.getId(), "driver.not_found");
        checkValidManualUpdateDriverStatus(user, editDriverPayload.getStatus());
        validUpdateDriverActiveStatus(user, editDriverPayload.getActive());

        driverValidation.validDriverRequireInfo(editDriverPayload, user.getUserGroup().getId());
        setDriverRelationInfo(user, editDriverPayload.getBranchId(),
                null, editDriverPayload.getUnitOperatorId());

        modelMapper.map(editDriverPayload, user);

        user.setMobileFull(PhoneNumberUtil.toMobileFullPattern(editDriverPayload.getCountryCode(),
                editDriverPayload.getMobile()));

        if (Objects.nonNull(editDriverPayload.getPassword())) {
            user.setPassword(passwordUtil.encryptPassword(editDriverPayload.getPassword()));
            user.setLoginFailedTimes(0);
            user.setLoginTimes(0);
        }

        checkExistDriverInfo(user);
        return save(user);
    }

    @Override
    public Boolean deleteDriver(Long id) {
        User user = findByIdIgnoreDelete(id);

        if (!user.getUserRole().getId().equals(DRIVER.getId())) {
            throw new BadRequestException("User is not driver");
        }

        if (user.isDeleted()) {
            throw new BadRequestException("Driver already deleted!!!")
                    .displayMessage(Translator.toLocale("driver.already_deleted"));
        }

        if (Objects.nonNull(user.getInContract()) && user.getInContract()) {
            throw new BadRequestException("Driver was assigned to a contract!!!")
                    .displayMessage(Translator.toLocale("driver.driver_is_current_in_a_contract"));
        }

        user.setDeleted(true);
        return true;
    }

    private void validUpdateDriverActiveStatus(User user, Boolean active) {
        if (!active && Objects.nonNull(user.getCurrentJourneyDiaryId())) {
            throw new BadRequestException("Cannot set driver account inactive!!!")
                    .displayMessage(Translator.toLocale("driver.valid_error.cannot_set_inactive"));
        }
    }

    private void checkValidManualUpdateDriverStatus(User currentUser, Integer newStatus) {
        if (!currentUser.getStatus().equals(newStatus)) {
            if (UNAVAILABLE.getValue().equals(currentUser.getStatus())) {
                if (Objects.nonNull(currentUser.getCurrentJourneyDiaryId())) {
                    throw new BadRequestException("User is current in a journey!!!")
                            .displayMessage(Translator.toLocale("driver.driver_is_current_in_journey_diary"));
                }

                if (APPOINTED.getValue().equals(newStatus)
                        && Objects.isNull(currentUser.getCurrentContractId())) {
                    throw new BadRequestException("Cannot set status from Unavailable to Appointed!!!")
                            .displayMessage(Translator.toLocale("driver.driver_is_not_assigned_to_any_contract"));
                }

                if (WAITING.getValue().equals(newStatus)
                        && !Objects.isNull(currentUser.getCurrentContractId())) {
                    throw new BadRequestException("Cannot set status from Unavailable to Waiting!!!")
                            .displayMessage(Translator.toLocale("driver.driver_is_current_in_a_contract"));
                }


            } else if (WAITING.getValue().equals(currentUser.getStatus())
                    && APPOINTED.getValue().equals(newStatus)) {
                throw new BadRequestException("Cannot manual set driver status from Waiting to Appointed!!!")
                        .displayMessage(Translator.toLocale("driver.avoid_manual_set_waiting_to_appointed_status"));
            } else if (APPOINTED.getValue().equals(currentUser.getStatus())
                    && WAITING.getValue().equals(newStatus)) {
                throw new BadRequestException("Cannot manual set driver status from Appointed to Waiting!!!")
                        .displayMessage(Translator.toLocale("driver.avoid_manual_set_appointed_to_waiting_status"));
            }

        }
    }

    private void setDriverRelationInfo(User user, Long branchId,
                                       Long userGroupId, Long unitOperatorId) {
        user.setBranch(branchService.findById(branchId));

        if (Objects.nonNull(userGroupId)) {
            user.setUserGroup(userGroupService.findById(userGroupId));
        }

        if (Objects.nonNull(unitOperatorId)) {
            user.setUnitOperator(findUnitOperatorById(unitOperatorId));
        }
    }

    private void checkDriverExistWithIdCardAndUserGroup(User savedUser) {
        if (userRepo.existsByIdCardAndCardTypeAndUserGroupIdAndIdNotAndUserRoleIdAndDeletedFalse(savedUser.getIdCard(), savedUser.getCardType(), savedUser.getUserGroup().getId(), savedUser.getId(), savedUser.getUserRole().getId())) {
            throw new BadRequestException("IdCard already exist!!!")
                    .displayMessage(Translator.toLocale("driver.valid_error.id_card_already_exist"));
        }
    }

    private void checkExistUserCode(String code, Long id) {
        if (userRepo.existsByCodeAndIdNot(code, id)) {
            throw new BadRequestException("Driver code already exist!!!")
                    .displayMessage(Translator.toLocale("driver.error.code_already_exist"));
        }
    }

    @Override
//    @Cacheable(cacheNames = "CreateDriverOptions", key = "#root.method.name")
    public CreateDriverOptionsWrapper getCreateDriverOptions() {
        List<UserGroupDTO> userGroupDTOS = listUtils.mapAll(userGroupService.findByDeletedFalse(), UserGroupDTO.class);
        List<BranchDTO> branchDTOS = branchService.getBranchInfos();
        return CreateDriverOptionsWrapper.builder()
                .branches(branchDTOS)
                .supplierGroups(userGroupDTOS)
                .build();
    }

    @Override
//    @Cacheable(cacheNames = "DriverFilterOptions", key = "#root.method.name")
    public DriverFilterOptionsWrapper getFilterOptions() {
        return DriverFilterOptionsWrapper.builder()
                .driverStatusEnums(DRIVER_STATUS_ENUMS)
                .supplierGroupDTOS(listUtils.mapAll(userGroupService.findByDeletedFalse(), UserGroupDTO.class))
                .build();
    }

    @Override
    public String suggestDriverCode(String prefixCode) {
        String code = userRepo.getNewestCodeByPrefix(prefixCode);

        if (Objects.isNull(code)) {
            return prefixCode + AvisApiConstant.DEFAULT_SUFFIX_CODE;
        } else {
            String suffixCode = code.split(prefixCode)[1];
            return String.format(prefixCode + "%04d", Integer.parseInt(suffixCode) + 1);
        }
    }

    @Override
    public User findById(Long id, String msgCode) {
        if (Objects.isNull(id)) {
            return null;
        }

        return userRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Cannot find user with id: " + id)
                        .displayMessage(Translator.toLocale(msgCode)));
    }

    @Override
    public User findOperationAdminById(Long id) {
        return Objects.isNull(id) ? null : userRepo.findByIdAndDepartmentIdAndActiveTrueAndDeletedFalse(id, OPERATION_ADMIN.getId())
                .orElseThrow(() -> new NotFoundException("Cannot find operation admin with id: " + id)
                        .displayMessage(Translator.toLocale("operation_admin.error.not_found")));
    }

    @Override
    public User findUnitOperatorById(Long id) {
        return Objects.isNull(id) ? null : userRepo.findByIdAndDepartmentIdAndActiveTrueAndDeletedFalse(id, UNIT_OPERATOR.getId())
                .orElseThrow(() -> new NotFoundException("Cannot find unit operator with id: " + id)
                        .displayMessage(Translator.toLocale("unit_operator.error.not_found")));
    }

    @Override
    public User findAccountantById(Long id) {
        return Objects.isNull(id) ? null : userRepo.findByIdAndDepartmentIdAndActiveTrueAndDeletedFalse(id, ACCOUNTANT.getId())
                .orElseThrow(() -> new NotFoundException("Cannot find accountant with id: " + id)
                        .displayMessage(Translator.toLocale("accountant.error.not_found")));
    }

    @Override
//    @Cacheable(cacheNames = "CreateAdminUserOptions", key = "#root.method.name")
    public CreateAdminUserOptionsWrapper getAdminUserCreateOptions() {
        return CreateAdminUserOptionsWrapper.builder()
                .branches(branchService.getBranchInfos())
                .userDepartmentDTOS(userDepartmentService.findAll())
                .build();
    }

    @Override
    public User createAdminUser(CreateAdminUserPayload payload, User user) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);

        User newUser = modelMapper.map(payload, User.class);
        newUser.setActive(true);
        newUser.setUsername(newUser.getCode());
        newUser.setPassword(passwordUtil.getAdminUserDefaultPass());
        newUser.setUserRole(userRoleService.findById(payload.getUserRoleId()));
        newUser.setBranch(Objects.nonNull(payload.getBranchId()) ? branchService.findById(payload.getBranchId()) : null);
        newUser.setCreatedBy(user);

        if (Objects.nonNull(payload.getDepartmentId())) {
            newUser.setDepartment(userDepartmentService.findById(payload.getDepartmentId()));
        }

        User savedUser = save(newUser);

        checkUserExistByUsernameAndIdNot(savedUser.getUsername(), savedUser.getId());
        checkExistUserCode(savedUser.getCode(), savedUser.getId());

        return savedUser;
    }

    @Override
    public User updateAdminUser(UpdateAdminUserPayload payload) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);

        User user = findById(payload.getId(), "user.error.not_found");

        if (StringUtils.isNotBlank(payload.getPassword())) {
            payload.setPassword(passwordUtil.encryptPassword(payload.getPassword()));
            user.setLoginTimes(0);
            user.setLoginFailedTimes(0);
        } else {
            payload.setPassword(user.getPassword());
        }

        modelMapper.map(payload, user);

        if (Objects.nonNull(payload.getBranchId())) {
            user.setBranch(branchService.findById(payload.getBranchId()));
        } else {
            user.setBranch(null);
        }
        user.setUserRole(userRoleService.findById(payload.getUserRoleId()));

        return save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    @Override
    public int updateBranchOfDriver(MultipartFile file) {
        if(file == null || file.isEmpty()) return -1;
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 1;
            List<User> users = new ArrayList<>();
            while (true){
                Row row = sheet.getRow(rowNum++);
                if(row == null) break;

                Cell username = row.getCell(2);
                if(StringUtils.isEmpty(username.getStringCellValue())) break;
                Cell branchOld = row.getCell(10);
                Cell branchNew = row.getCell(11);
                if(branchOld != null && branchNew != null){
                    String usernameTx = username.getStringCellValue();
                    String branchOldTx = branchOld.getStringCellValue();
                    String branchNewTx = branchNew.getStringCellValue();


                    User user = userRepo.findByUsername(usernameTx);
                    if(user != null && StringUtils.isNotEmpty(branchNewTx) && !branchOldTx.equals(branchNewTx)){
                        System.out.println("------Username-----");
                        System.out.println(usernameTx);
                        Branch branch = branchService.findByCode(branchNewTx);
                        if(branch != null){
                            user.setBranch(branch);
                            users.add(user);
                        }
                    }
                }
            }

            userRepo.saveAll(users);
            return users.size();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void checkUserExistByUsernameAndIdNot(String username, Long id) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Username cannot be blank!!!")
                    .displayMessage(Translator.toLocale("user.error.username_is_invalid"));
        }

        if (userRepo.existsByUsernameAndIdNot(username, id)) {
            throw new BadRequestException("Username already exist!!!")
                    .displayMessage(Translator.toLocale("user.error.username_already_exist"));
        }
    }

    @Override
    public void checkUserExistsByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Username cannot be blank!!!")
                    .displayMessage(Translator.toLocale("user.error.username_is_invalid"));
        }
        if(userRepo.existsByUsername(username)){
            throw new BadRequestException("Username already exist!!!")
                    .displayMessage(String.format(Translator.toLocale("user.error.username_already_exist_code"), username));
        }
    }
}
