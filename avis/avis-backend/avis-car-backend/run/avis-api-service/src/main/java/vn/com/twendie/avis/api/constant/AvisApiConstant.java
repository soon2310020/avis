package vn.com.twendie.avis.api.constant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import vn.com.twendie.avis.data.enumtype.*;

import java.util.List;
import java.util.Map;

import static vn.com.twendie.avis.data.enumtype.ContractCostTypeEnum.*;

public class AvisApiConstant {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final String DEFAULT_STARTER_PAGE_STRING = "1";
    public static final String DEFAULT_PAGE_SIZE_STRING = "10";
    public static final int DEFAULT_STARTER_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SUFFIX_CODE = "0001";

    public static final Map<Long, String> PARKING_PLACE_MAP = ImmutableMap.of(
            ParkingPlaceEnum.HOME.getId(), ParkingPlaceEnum.HOME.getName(),
            ParkingPlaceEnum.AVIS.getId(), ParkingPlaceEnum.AVIS.getName()
    );

    public static final Map<String, Long> CONTRACT_TYPE = ImmutableMap.of(
            "contract_with_driver", ContractTypeEnum.WITH_DRIVER.value(),
            "contract_without_driver", ContractTypeEnum.WITHOUT_DRIVER.value()
    );

    public static final Map<String, String> MEMBER_CUSTOMER_PREFIX_CODE_MAPPING = ImmutableMap.of(
            MemberCustomerRoleEnum.ADMIN.getCode(), PrefixCode.ADMIN_MEMBER,
            MemberCustomerRoleEnum.USER.getCode(), PrefixCode.USER_MEMBER
    );

    public static final Map<Long, String> CONTRACT_TYPE_REVERSE = ImmutableMap.of(
            ContractTypeEnum.WITH_DRIVER.value(), "contract_with_driver",
            ContractTypeEnum.WITHOUT_DRIVER.value(), "contract_without_driver"
    );

    public static final List<Long> AVIS_DRIVER_GROUP = ImmutableList.of(
            DriverGroupEnum.AVIS.getId(),
            DriverGroupEnum.SUB_CONTRACTOR.getId()
    );

    public static final List<VehicleStatusEnum> VEHICLE_STATUS_ENUMS = ImmutableList.of(
            VehicleStatusEnum.UNAVAILABLE,
            VehicleStatusEnum.WAITING,
            VehicleStatusEnum.APPOINTED
    );

    public static final List<DriverStatusEnum> DRIVER_STATUS_ENUMS = ImmutableList.of(
            DriverStatusEnum.UNAVAILABLE,
            DriverStatusEnum.WAITING,
            DriverStatusEnum.APPOINTED
    );

    public static final List<String> PR_COST_CODES = ImmutableList.of(
            OVER_KM_SURCHARGE.code(),
            OVERNIGHT_SURCHARGE.code(),
            SELF_DRIVE_OVER_KM_SURCHARGE.code(),
            SELF_DRIVE_NORMAL_DAY_SURCHARGE.code(),
            SELF_DRIVE_WEEKEND_SURCHARGE.code(),
            SELF_DRIVE_HOLIDAY_SURCHARGE.code(),
            WEEKEND_SURCHARGE.code(),
            HOLIDAY_SURCHARGE.code(),
            OVERTIME_SURCHARGE.code()
    );

    public static final List<String> TICKET_FEE_REPORT_COST_CODES = ImmutableList.of(
            JourneyDiaryCostTypeEnum.TOLLS_FEE.code(),
            JourneyDiaryCostTypeEnum.PARKING_FEE.code(),
            JourneyDiaryCostTypeEnum.TIRE_REPAIR_FEE.code(),
            JourneyDiaryCostTypeEnum.CAR_WASH_FEE.code()
    );

    public interface BooleanStringValue {
        String TRUE = "true";
        String FALSE = "false";
    }

    public interface PrefixCode {
        String DRIVER = "TX";
        String ADMIN_MEMBER = "AD";
        String CUSTOMER = "KH";
        String USER_MEMBER = "US";
        String ADMIN_USER = "NV";
    }

    public interface AlertTime {
        long DRIVER_LICENSE_EXPIRY_DATE = 60;
        long VEHICLE_REGISTRATION_TO_DATE = 30;
        long VEHICLE_TRAVEL_WARRANT_EXPIRY_DATE = 30;
    }
}
