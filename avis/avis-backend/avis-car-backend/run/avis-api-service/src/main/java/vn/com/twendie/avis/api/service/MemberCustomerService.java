package vn.com.twendie.avis.api.service;

import org.springframework.data.domain.Page;
import vn.com.twendie.avis.api.model.filter.AdminCustomerFilter;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.model.response.MemberCustomerDTO;
import vn.com.twendie.avis.api.repository.AdminCustomerProjection;
import vn.com.twendie.avis.data.model.MemberCustomer;

import java.util.List;
import java.util.Optional;

public interface MemberCustomerService {

    MemberCustomer findById(Long id);

    Optional<MemberCustomer> findByIdAndCustomerId(Long memberId, Long customerId);

    Page<AdminCustomerProjection> filterAdminCustomers(FilterWrapper<AdminCustomerFilter> filter);

    Boolean existByPhoneNumberAndCountryCode(String phoneNumber, String countryCode);

    Boolean existByPhoneNumberAndCountryCodeAndIdNot(String phoneNumber, String countryCode, Long id);

    Boolean existByPhoneNumberAndCountryCodeAndIdNotAndParentIdNotNull(String phoneNumber, String countryCode, Long id);

    Boolean existByEmailAndIdNot(String email, Long id);

    MemberCustomer save(MemberCustomer memberCustomer);

    Boolean deleteMemberCustomer(Long id);

    void updateInContract(MemberCustomer memberCustomer);

    String suggestAdminMemberCode();

    String suggestUserSignatureCode(int count);

    List<String> generateMemberCode(String role, int count);

    Boolean existsByCodeAndIdNot(String code, Long id);

    Boolean isLastActiveAdmin(MemberCustomer admin);

    void migrateUserSignature();

    List<MemberCustomerDTO> suggestionMemberCustomerIgnore(Long memberCustomerId, String name);

    List<MemberCustomerDTO> suggestionMemberCustomer(String name, Long contractId);

    List<String> getNameByIdIn(List<Long> ids);
}
