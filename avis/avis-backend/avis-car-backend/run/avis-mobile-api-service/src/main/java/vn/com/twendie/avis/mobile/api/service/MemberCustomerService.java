package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.MemberCustomer;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

public interface MemberCustomerService {

    List<String> generateMemberCode(int count);

    MemberCustomer save(MemberCustomer memberCustomer);

    int countByNameAndDepartment(String name, String department);

    Optional<MemberCustomer> findById(Long id);

    Optional<MemberCustomer> findByUserId(Long userId);

    List<MemberCustomer> findByUserIdIn(List<Long> userIds);

}
