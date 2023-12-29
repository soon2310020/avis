package saleson.api.user;

import java.util.List;

import org.springframework.data.domain.Pageable;

import saleson.api.user.payload.UserParam;
import saleson.common.enumeration.CompanyType;
import saleson.common.hibernate.CustomRepository;
import saleson.model.User;

public interface UserRepositoryCustom<T, P> extends CustomRepository<T, P> {
	List<User> findByIdInAndCompanyType(List<Long> ids, CompanyType companyType);

	List<User> findAllUserActiveByEmailIn(List<String> emailList, String searchText, Pageable pageable);

//	Page<UserLite> findByCompanyIdAndQueryAndAvailble(Long companyId, String query, List<Long> userIds, Pageable pageable);
//
//	Page<UserLite> findByCompanyIdAndQueryAndAvailble2(Long companyId, String query, List<Long> userIds, Pageable pageable);

	long countAll(UserParam searchParam);

	List<Long> findAllIdByParam(UserParam searchParam);

	List<User> findByCompanyTypeAndEnabledIsTrue(CompanyType companyType);

	List<User> findByCompanyTypeAndIsEmoldinoIsTrueAndEnabledIsTrue(CompanyType companyType);
}
