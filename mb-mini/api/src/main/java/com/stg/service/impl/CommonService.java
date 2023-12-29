package com.stg.service.impl;

import com.google.gson.Gson;
import com.stg.entity.customer.Customer;
import com.stg.entity.customer.CustomerDetail;
import com.stg.entity.user.User;
import com.stg.errors.ApplicationException;
import com.stg.errors.UserHasNoPermissionException;
import com.stg.errors.UserNotFoundException;
import com.stg.repository.CustomerDetailRepository;
import com.stg.repository.UserRepository;
import com.stg.service.dto.external.responseFlexible.FlexibleQuoteRespDto;
import com.stg.utils.DateUtil;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.stg.utils.CommonMessageError.MSG12;

@Service
@Slf4j
public class CommonService {
    @Value("${spring.redis.cache.common-service.process-email-expire-time}")
    public int processEmailExpireTime;
    @Value("${spring.redis.cache.common-service.process-data-expire-time}")
    public int processDataExpireTime;
    @Autowired
    private static UserRepository userRepository;

    @Autowired
    private final RedisCommands<String, String> redis;

    @Autowired
    private final Gson gson;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final CustomerDetailRepository customerDetailRepository;

    public CommonService(CustomerDetailRepository customerDetailRepository,
                         RedisCommands<String, String> redis,
                         Gson gson, ModelMapper modelMapper) {
        this.customerDetailRepository = customerDetailRepository;
        this.redis = redis;
        this.gson = gson;
        this.modelMapper = modelMapper;
    }

    public static void isSuperAdminAndAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    public void setProcessCacheData(String key, String value) {
        redis.set(key, value, SetArgs.Builder.ex(processDataExpireTime)); // 30 mins
    }


    public String getProcessCacheData(String key) {
        String cacheValue = redis.get(key);
        if (cacheValue == null) {
            log.error("[MINI]--Dữ liệu cache cho key {} không tồn tại", key);
            throw new ApplicationException(MSG12);
        }
        return cacheValue;
    }

    public void setEmailProcessCacheData(String key, String value) {
        redis.set(key, value, SetArgs.Builder.ex(processEmailExpireTime));
    }

    public void setProcessCacheFlexibleTime(String key, String value, int expiredTimeSecond) {
        redis.set(key, value, SetArgs.Builder.ex(expiredTimeSecond));
    }

    public String getProcessCacheDataNotThrow(String key) {
        return redis.get(key);
    }

    public String convertObjectToJson(Object obj) {
        return gson.toJson(Objects.requireNonNullElse(obj, ""));
    }

    private static User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    public CustomerDetail generateCustomerDetail(Customer customer, Object reqDto) {
        // saving customer detail
        CustomerDetail customerDetail = modelMapper.map(customer, CustomerDetail.class);

        if (reqDto instanceof FlexibleQuoteRespDto) {
            FlexibleQuoteRespDto flexibleProcessResp = (FlexibleQuoteRespDto) reqDto;
            customerDetail.setAnnualIncome(flexibleProcessResp.getCustomer().getAnnualIncome());
            customerDetail.setBirthday(DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, flexibleProcessResp.getCustomer().getDob() + " 00:00:00"));
            customerDetail.setAddress(flexibleProcessResp.getCustomer().getAddress().fullAddress()); // fullAddress info
            customerDetail.setEmail(flexibleProcessResp.getCustomer().getEmail());
            customerDetail.setGender(flexibleProcessResp.getCustomer().getGender().name());
            customerDetail.setPhone(flexibleProcessResp.getCustomer().getPhoneNumber());
            customerDetail.setIdentification(flexibleProcessResp.getCustomer().getIdentificationNumber());
            customerDetail.setIdCardType(flexibleProcessResp.getCustomer().getIdentificationType().name());
            customerDetail.setId(null);
        }
        return customerDetail;
    }
}
