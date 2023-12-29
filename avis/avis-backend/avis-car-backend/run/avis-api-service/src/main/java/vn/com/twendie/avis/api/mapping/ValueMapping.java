package vn.com.twendie.avis.api.mapping;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface ValueMapping<T> {

    ValueMapping<Object> DEFAULT_VALUE_MAPPING = new DefaultValueMapping();
    ValueMapping<String> BOOLEAN_VALUE_MAPPING = new BooleanValueMapping();
    ValueMapping<String> DATE_VALUE_MAPPING = new DateValueMapping();
    ValueMapping<String> TIME_VALUE_MAPPING = new TimeValueMapping();
    ValueMapping<String> STRING_COLLECTION_MAPPING = new StringCollectionMapping();

    Map<String, ValueMapping<?>> VALUE_MAPPINGS = new HashMap<String, ValueMapping<?>>() {{
        put("Contract.status", new ContractStatusMapping());
        put("Contract.vatTollFee", new ContractVatTollFeeMapping());
        put("Vehicle.status", new VehicleStatusMapping());
        put("Vehicle.transmissionType", new VehicleTransmissionTypeMapping());
        put("getCustomerCardType", new IdCardTypeMapping());
        put("User.status", new DriverStatusMapping());
        put("User.cardType", new IdCardTypeMapping());
        put("Customer.cardType", new IdCardTypeMapping());
        put("Contract.parkingId", new ContractParkingMapping());
        put("Contract.timeUsePolicyGroupId", new UsingTimeUsePolicyMapping());
        put("JourneyDiaryDailyDTO.overTime", new OverTimeMapping());
        put("JourneyDiaryDailyDTO.isHoliday", new BooleanToSymbolMapping("x"));
        put("JourneyDiaryDailyDTO.isWeekend", new BooleanToSymbolMapping("x"));
        put("JourneyDiaryDailyDTO.getBoolSelfDrive", new BooleanToSymbolMapping("x"));
        put("JourneyDiaryDailyDTO.getBoolWithDriver", new BooleanToSymbolMapping("x"));
        put("PaymentRequestItemDTO.count", new CountValueToSymbolMapping("0"));
    }};

    static ValueMapping<?> getValueMapping(String key) {
        return VALUE_MAPPINGS.getOrDefault(key, DEFAULT_VALUE_MAPPING);
    }

    @SneakyThrows
    static ValueMapping<?> getValueMapping(Field field) {
        if (field.isAnnotationPresent(Mapping.class)) {
            return field.getAnnotation(Mapping.class).value().newInstance();
        }
        String key = field.getDeclaringClass().getSimpleName() + "." + field.getName();
        return getValueMapping(key);
    }

    @SneakyThrows
    static ValueMapping<?> getValueMapping(Method method) {
        if (method.isAnnotationPresent(Mapping.class)) {
            return method.getAnnotation(Mapping.class).value().newInstance();
        }
        String key = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        return getValueMapping(key);
    }

    T map(Object value);

}
