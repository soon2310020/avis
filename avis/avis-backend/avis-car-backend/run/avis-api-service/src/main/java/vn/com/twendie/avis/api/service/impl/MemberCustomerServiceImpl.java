package vn.com.twendie.avis.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.filter.AdminCustomerFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.response.MemberCustomerDTO;
import vn.com.twendie.avis.api.repository.AdminCustomerProjection;
import vn.com.twendie.avis.api.repository.MemberCustomerRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.util.PasswordUtil;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.UserRole;
import vn.com.twendie.avis.locale.config.Translator;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.*;

@Service
@CacheConfig(cacheNames = "MemberCustomer")
public class MemberCustomerServiceImpl implements MemberCustomerService {

    private final MemberCustomerRepo memberCustomerRepo;
    private final ContractService contractService;
    private final CustomerService customerService;
    private final PasswordUtil passwordUtil;
    private final DateUtils dateUtils;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final ModelMapper modelMapper;
    public MemberCustomerServiceImpl(MemberCustomerRepo memberCustomerRepo,
                                     ContractService contractService,
                                     CustomerService customerService,
                                     DateUtils dateUtils,
                                     PasswordUtil passwordUtil,
                                     UserService userService,
                                     UserRoleService userRoleService,
                                     ModelMapper modelMapper) {
        this.memberCustomerRepo = memberCustomerRepo;
        this.contractService = contractService;
        this.customerService = customerService;
        this.dateUtils = dateUtils;
        this.passwordUtil = passwordUtil;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.modelMapper = modelMapper;
    }

    @Override
//    @Cacheable(key = "#p0", condition = "#p0 != null")
    public MemberCustomer findById(Long id) {
        return Objects.isNull(id) ? null : memberCustomerRepo.findByIdAndDeletedFalse(id).orElseThrow(() ->
                new NotFoundException("Not found member_customer with id: " + id)
                        .displayMessage(Translator.toLocale("error.customer_member.not_found")));
    }

    @Override
    public Optional<MemberCustomer> findByIdAndCustomerId(Long memberId, Long customerId) {
        return memberCustomerRepo.findByIdAndCustomerIdAndDeletedFalse(memberId, customerId);
    }

    @Override
    public Page<AdminCustomerProjection> filterAdminCustomers(FilterWrapper<AdminCustomerFilter> filterWrapper) {
        AdminCustomerFilter filter = filterWrapper.getFilter();

        Timestamp startOfDay = dateUtils.startOfDay(filter.getCreatedAt());
        Timestamp endOfDay = dateUtils.endOfDay(filter.getCreatedAt());

        return memberCustomerRepo.filterAdminCustomers(
                normalize(defaultIfBlank(filter.getCustomerName(), "%")),
                isBlank(filter.getTaxCode()) ? null : normalize(filter.getTaxCode()),
                normalize(defaultIfBlank(filter.getAddress(), "%")),
                isBlank(filter.getAdminName()) ? null : normalize(filter.getAdminName()),
                isBlank(filter.getCreatedBy()) ? null : normalize(filter.getCreatedBy()),
                startOfDay,
                endOfDay,
                PageRequest.of(filterWrapper.getPage() - 1, filterWrapper.getSize())
        );
    }

    @Override
    public Boolean existByPhoneNumberAndCountryCode(String phoneNumber, String countryCode) {
        return memberCustomerRepo.existsByMobileAndCountryCodeAndDeletedFalse(phoneNumber, countryCode);
    }

    @Override
    public Boolean existByPhoneNumberAndCountryCodeAndIdNot(String phoneNumber, String countryCode, Long id) {
        return memberCustomerRepo.existsByMobileAndCountryCodeAndIdNotAndDeletedFalse(phoneNumber, countryCode, id);
    }

    @Override
    public Boolean existByPhoneNumberAndCountryCodeAndIdNotAndParentIdNotNull(String phoneNumber, String countryCode, Long id) {
        return memberCustomerRepo.countByMobileAndCountryCodeAndIdNotAndDeletedFalseAndParentIdNotNull(phoneNumber, countryCode, id) > 0;
    }

    @Override
    public Boolean existByEmailAndIdNot(String email, Long id) {
        return memberCustomerRepo.countByEmailAndIdNotAndDeletedFalse(email, id) > 0;
    }

    @Override
    public MemberCustomer save(MemberCustomer memberCustomer) {
        return memberCustomerRepo.save(memberCustomer);
    }

    @Override
    public Boolean deleteMemberCustomer(Long id) {
        MemberCustomer memberCustomer = findById(id);

        if (memberCustomer.isDeleted()) {
            throw new BadRequestException("Member customer already deleted!!!")
                    .displayMessage(Translator.toLocale("error.customer.already_deleted"));
        }

        if (memberCustomer.isInContract()) {
            throw new BadRequestException("Member customer still exist in contract!")
                    .displayMessage(Translator.toLocale("error.customer.cannot_delete_within_contract"));
        }

        if (MemberCustomerRoleEnum.USER.getCode().equals(memberCustomer.getRole())) {
            memberCustomer.setDeleted(true);
        } else {
            List<Long> memberIds = memberCustomer.getChildren().stream()
                    .map(MemberCustomer::getId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(memberIds)) {
                if (contractService.existByListMemberCustomerIdAndDeletedFalse(memberIds)) {
                    throw new BadRequestException("Member customer still exist in contract!")
                            .displayMessage(Translator.toLocale("error.admin_customer.cannot_delete_within_contract"));
                }
            }

            memberCustomer.setDeleted(true);

            if (Objects.nonNull(memberCustomer.getUser())) {
                memberCustomer.getUser().setDeleted(true);
            }

            memberCustomer.getChildren().forEach(x -> x.setDeleted(true));

            deleteCustomerIfAllMemberAreDeleted(memberCustomer.getCustomer().getId());

        }
        return true;
    }

    @Override
    public void updateInContract(MemberCustomer memberCustomer) {
        if (Objects.nonNull(memberCustomer) && !memberCustomer.isInContract()) {
            memberCustomer.setInContract(true);
            save(memberCustomer);
        }
    }

    @Override
    public String suggestAdminMemberCode() {
        String prefixCode = AvisApiConstant.PrefixCode.ADMIN_MEMBER;
        String code = memberCustomerRepo.getNewestCodeByPrefix(prefixCode);

        if (Objects.isNull(code)) {
            return prefixCode + AvisApiConstant.DEFAULT_SUFFIX_CODE;
        } else {
            String suffixCode = code.split(prefixCode)[1];
            return String.format(prefixCode + "%04d", Integer.parseInt(suffixCode) + 1);
        }
    }

    @Override
    public String suggestUserSignatureCode(int count) {
        if(count < 0) count = 0;
        String prefixCode = AvisApiConstant.PrefixCode.USER_MEMBER;
        String code = memberCustomerRepo.getNewestCodeByPrefix(prefixCode);

        if(Objects.isNull(code)){
            return prefixCode + AvisApiConstant.DEFAULT_SUFFIX_CODE;
        }else{
            String suffixCode = code.split(prefixCode)[1];
            suffixCode = String.valueOf(Integer.parseInt(suffixCode) + count);
            return String.format(prefixCode + "%04d", Integer.parseInt(suffixCode) + 1);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> generateMemberCode(String role, int count) {
        String prefixCode = AvisApiConstant.MEMBER_CUSTOMER_PREFIX_CODE_MAPPING.get(role);
        String code = memberCustomerRepo.getNewestCodeByPrefix(prefixCode);
        List<String> result = new ArrayList<>();
        int suffixNumber;

        if (Objects.isNull(code)) {
            code = prefixCode + AvisApiConstant.DEFAULT_SUFFIX_CODE;
            result.add(code);
            String suffixCode = code.split(prefixCode)[1];
            suffixNumber = Integer.parseInt(suffixCode);
        } else {
            String suffixCode = code.split(prefixCode)[1];
            suffixNumber = Integer.parseInt(suffixCode) + 1;
            code = String.format(prefixCode + "%04d", suffixNumber);
            result.add(code);
        }

        if (count > 1) {
            for (int i = 1; i < count; i++) {
                result.add(String.format(prefixCode + "%04d", suffixNumber + i));
            }
        }

        return result;
    }

    @Override
    public Boolean existsByCodeAndIdNot(String code, Long id) {
        return memberCustomerRepo.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Boolean isLastActiveAdmin(MemberCustomer admin) {
        Boolean result = memberCustomerRepo.existsByCustomerIdAndDeletedFalseAndRoleAndIdNotAndActiveTrue(admin.getCustomer().getId(),
                MemberCustomerRoleEnum.ADMIN.getCode(), admin.getId());
        return Objects.isNull(result) || !result;
    }

    @Override
    public void migrateUserSignature() {
        List<MemberCustomer> memberCustomers = memberCustomerRepo.findByUserIsNull();
        memberCustomers.forEach(e ->{
            String userCode = suggestUserSignatureCode(0);

            User user = new User();
            user.setCode(userCode);
            user.setUsername(userCode);
            user.setName(e.getName());
            user.setActive(true);
            user.setPassword(passwordUtil.getSignatureDefaultPassword());
            user.setUserRole(userRoleService.findById(UserRoleEnum.SIGNATURE.getId()));
            e.setCode(userCode);

            userService.save(user);

            e.setUser(user);
        });

        memberCustomerRepo.saveAll(memberCustomers);
    }

    @Override
    public List<MemberCustomerDTO> suggestionMemberCustomerIgnore(Long memberCustomerId, String name) {
        return memberCustomerRepo.findByParentIdAndIsDeletedIsFalseAndRoleIsAndNameLike(MemberCustomerRoleEnum.IGNORE.getCode(),
                memberCustomerId, name)
                .stream().map(m -> modelMapper.map(m, MemberCustomerDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<MemberCustomerDTO> suggestionMemberCustomer(String name, Long contractId) {
        if(name == null) name = "";
        if(contractId == null) {
            return memberCustomerRepo.getMemberCustomerByName(name, MemberCustomerRoleEnum.IGNORE.getCode())
                    .stream().map(m -> modelMapper.map(m, MemberCustomerDTO.class))
                    .collect(Collectors.toList());
        }else {
            return memberCustomerRepo.getMemberCustomerByNameAndContractId(name, MemberCustomerRoleEnum.IGNORE.getCode(), contractId)
                    .stream()
                    .map(m -> modelMapper.map(m, MemberCustomerDTO.class))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getNameByIdIn(List<Long> ids) {
        return memberCustomerRepo.findNameByIdIn(ids);
    }

    private void deleteCustomerIfAllMemberAreDeleted(Long id) {
        Customer customer = customerService.findById(id);
        if (!memberCustomerRepo.existsByCustomerIdAndDeletedFalse(id)) {
            customer.setDeleted(true);
        }
    }

    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

}
