package vn.com.twendie.avis.api.service;

import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.model.payload.*;
import vn.com.twendie.avis.api.model.projection.UserProjection;
import vn.com.twendie.avis.api.model.response.CreateAdminUserOptionsWrapper;
import vn.com.twendie.avis.api.model.response.CreateContractDriverDTO;
import vn.com.twendie.avis.api.model.response.CreateDriverOptionsWrapper;
import vn.com.twendie.avis.api.model.response.DriverFilterOptionsWrapper;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.User;

import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

public interface UserService {

    User findDriverByIdAndGroups(Long id, DriverGroupEnum... groups);

    Map<Long, UserProjection> findByIdIn(Collection<Long> ids);

    GeneralPageResponse<CreateContractDriverDTO> driverSuggestionsByName(DriverSuggestionPayload payload,
                                                                         boolean forContractWithDriver);

    User findByIdIgnoreDelete(Long id);

    User save(User user);

    void assignToContract(User driver, Contract contract);

    void unAssignFromContract(User driver, Contract contract);

    void checkUserExistByUsernameAndIdNot(String username, Long id);

    void checkUserExistsByUsername(String username);

    void updateDriverStatus(User driver);

    User getContractDriverAtTime(Contract contract, Timestamp timestamp);

    User createDriver(CreateDriverPayload createDriverPayload);

    User updateDriver(EditDriverPayload editDriverPayload);

    Boolean deleteDriver(Long id);

    CreateDriverOptionsWrapper getCreateDriverOptions();

    DriverFilterOptionsWrapper getFilterOptions();

    String suggestDriverCode(String prefixCode);

    User findById(Long id, String msgCode);

    User findOperationAdminById(Long id);

    User findUnitOperatorById(Long id);

    User findAccountantById(Long id);

    CreateAdminUserOptionsWrapper getAdminUserCreateOptions();

    User createAdminUser(CreateAdminUserPayload payload, User user);

    User updateAdminUser(UpdateAdminUserPayload payload);

    User findById(Long id);

    int updateBranchOfDriver(MultipartFile file);

}
