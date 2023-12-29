package com.stg.utils.address;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

import static com.stg.utils.NlpUtil.removeAccent;
import static com.stg.utils.address.AddressMark.AddressType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtil {
    private static final String PREFIX_PROVINCE = "(TỈNH|THÀNH PHỐ|THANH PHO|TP)";
    private static final String PREFIX_DISTRICT = "(HUYỆN|THỊ XÃ|QUẬN|THÀNH PHỐ|THI XA|THANH PHO|TX|TP)";
    private static final String PREFIX_WARD = "(XÃ|THỊ TRẤN|PHƯỜNG|TT)";

    private static final String WARD_SEPARATOR = "(XA |THI TRAN |PHUONG |TT )";
    private static final String DISTRICT_SEPARATOR = "(HUYEN |THI XA |QUAN |THANH PHO |TX |TP |Q )";
    private static final String PROVINCE_SEPARATOR = "(TINH |THANH PHO |TP )";
    public static final String ADDRESS_SEPARATOR = "(?=("
            + WARD_SEPARATOR.substring(1, WARD_SEPARATOR.length() - 1)
            + DISTRICT_SEPARATOR.substring(1, DISTRICT_SEPARATOR.length() - 1)
            + PROVINCE_SEPARATOR.substring(1, PROVINCE_SEPARATOR.length() - 1)
            +"))";

    private static final Pattern WARD_PATTERN = Pattern.compile("^" + WARD_SEPARATOR +".*$");
    private static final Pattern DISTRICT_PATTERN = Pattern.compile("^" + DISTRICT_SEPARATOR +".*$");
    private static final Pattern PROVINCE_PATTERN = Pattern.compile("^" + PROVINCE_SEPARATOR +".*$");

    /*public static final Pattern SPLIT_WARD_PATTERN = Pattern.compile("(?=" + WARD_SEPARATOR +")");
    public static final Pattern SPLIT_DISTRICT_PATTERN = Pattern.compile("(?=" + DISTRICT_SEPARATOR +")");
    public static final Pattern SPLIT_PROVINCE_PATTERN = Pattern.compile("(?=" + PROVINCE_SEPARATOR +")");*/
    public static final Pattern SPLIT_ADDRESS_PATTERN = Pattern.compile(ADDRESS_SEPARATOR);


    /***/
    public static String cleanUpperProvince(String s) {
        return removeAccent(s.toUpperCase().replaceAll(PREFIX_PROVINCE, "").trim());
    }

    /***/
    public static String cleanUpperDistrict(String s) {
        return removeAccent(s.toUpperCase().replaceAll(PREFIX_DISTRICT, "").trim());
    }

    /***/
    public static String cleanUpperWard(String s) {
        return removeAccent(s.toUpperCase().replaceAll(PREFIX_WARD, "").trim());
    }

    public static void main(String[] args) {
        System.out.println(cleanUpperProvince("Thành phố Hồ Chí Minh"));
        System.out.println(cleanUpperDistrict("Thành phố Phúc Yên"));
        System.out.println(cleanUpperWard("Thị trấn Sóc Sơn"));
    }

    /**
     * Continue...develop
     */
    public static String detectAddress(String address) {
        if (!StringUtils.hasText(address)) return null;

        address = address.trim().toUpperCase().replace("VIET NAM", "");
        String[] values = address.split("(-|!-)");

        StringBuilder outputBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String s : values) {
            if (!StringUtils.hasText(s)) {
                System.out.println("continue...");
                continue;
            }

            if (isFirst) {
                outputBuilder.append(s.trim());
                isFirst = false;
            } else {
                outputBuilder.append("|").append(s.trim());
            }
        }
        System.out.println(values.length + "||" + outputBuilder);
        return outputBuilder.toString();
    }

    /**
     * clean & uppercase
     */
    public static String cleanAddress(String address) {
        return address.toUpperCase().replaceAll("(-PHUONG CAT LINH-QUAN DONG DA-THANH PHO HA NOI-VIET NAM|-VIET NAM DA-THANH PHO HA NOI-VIET NAM|-VIET NAM NOI-VIET NAM|-VIET NAM)", "");
    }

    public static String cleanCountry(String address) {
        return address.replaceAll("(-VIET NAM)", "");
    }


    /**
     * MAIN FUNCTION
     */
    public static AddressInfo splitAddress(String address) {
        if (!StringUtils.hasText(address)) return new AddressInfo();

        address = cleanAddress(address); // clean & uppercase

        if (address.contains("!-")) {
            address = address.replaceFirst("(!-)", "");
            String[] arrays = address.split("(-)");
            return joinToObject(arrays);
        }

        if (address.contains("-")) {
            String[] arrays = address.split("(-)");
            return joinToObject(arrays);
        }

        if (address.contains(",")) {
            String[] arrays = address.split("(,)");
            return joinToObject(arrays);
        }

        if (address.contains("  ")) {
            String[] arrays = address.split("( {2})");
            return joinToObject(arrays);
        }

        /*String[] arrays = address.split(ADDRESS_SEPARATOR);
        System.out.println(address + " || " + joinToObject(arrays));*/
        return joinToObject(address);
    }

    ////////////////////////////////////////////////// VER 1 //////////////////////////////////////////////////
    public static AddressInfo joinToObject(String... arrays) {
        if (arrays.length > 4) {
            StringBuilder streetBuilder = new StringBuilder();
            boolean isFirst = true;
            for (int i = 0; i < arrays.length - 3; i++) {
                if (isFirst) {
                    isFirst = false;
                    streetBuilder.append(arrays[i]);
                } else {
                    streetBuilder.append("-").append(arrays[i]);
                }
            }
            arrays = new String[]{streetBuilder.toString(), arrays[arrays.length - 3], arrays[arrays.length - 2], arrays[arrays.length - 1]};
        }

        AddressMark[] addressMarks = new AddressMark[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            if (i == 0) addressMarks[i] = detectType(arrays[i].trim(), AddressType.UNKNOWN, i);
            else addressMarks[i] = detectType(arrays[i].trim(), addressMarks[i - 1].getType(), i);
        }

        return mapping2Object(addressMarks);
    }

    static AddressInfo mapping2Object(AddressMark... addressMarks) {
        AddressInfo addressInfo = new AddressInfo();

        String val;
        for (AddressMark addressMark : addressMarks) {
            switch (addressMark.getType()) {
                case WARD:
                    val = addressInfo.getWardName() != null ? addressInfo.getWardName() + "-" + addressMark.getValue() : addressMark.getValue();
                    addressInfo.setWardName(val);
                    break;
                case DISTRICT:
                    val = addressInfo.getDistrictName() != null ? addressInfo.getDistrictName() + "-" + addressMark.getValue() : addressMark.getValue();
                    addressInfo.setDistrictName(val);
                    break;
                case PROVINCE:
                    val = addressInfo.getProvinceName() != null ? addressInfo.getProvinceName() + "-" + addressMark.getValue() : addressMark.getValue();
                    addressInfo.setProvinceName(val);
                    break;
                case STREET:
                default:
                    val = addressInfo.getStreetName() != null ? addressInfo.getStreetName() + "-" + addressMark.getValue() : addressMark.getValue();
                    addressInfo.setStreetName(val);
            }
        }

        return addressInfo;
    }

    static AddressMark detectType(String arg, AddressType beforeType, int idx) {
        // WARNING: in order of priority and don't break!
        switch (beforeType) { //NOSONAR
            case UNKNOWN:
            case STREET: //NOSONAR
                if (WARD_PATTERN.matcher(arg).matches()) {
                    return new AddressMark(AddressType.WARD, arg);
                }
            case WARD: //NOSONAR
                if (DISTRICT_PATTERN.matcher(arg).matches()) {
                    return new AddressMark(AddressType.DISTRICT, arg);
                }
            case DISTRICT:
                if (PROVINCE_PATTERN.matcher(arg).matches()) {
                    return new AddressMark(AddressType.PROVINCE, arg);
                }
        }

        if (idx > 1) {
            switch (beforeType) {
                case WARD:
                    return new AddressMark(AddressType.DISTRICT, arg);
                case DISTRICT:
                case PROVINCE:
                    return new AddressMark(AddressType.PROVINCE, arg);
            }
        }

        return new AddressMark(AddressType.UNKNOWN, arg);
    }


    ////////////////////////////////////////////////// VER 2 //////////////////////////////////////////////////

    /*public static AddressInfo joinToObject2(String... arrays) {
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = arrays[i].trim();
        }

        switch (arrays.length) {
            case 4:
                return mapping2Object(arrays[0], arrays[1], arrays[2], arrays[3]);
            case 3:
                return mapping2Object(null, arrays[0], arrays[1], arrays[2]);
            case 2:
                return mapping2Object(null, null, arrays[0], arrays[1]);
            case 1:
                return mapping2Object(null, null, null, arrays[0]);
            case 0:
                return new AddressInfo();

            default: // length > 4
                StringBuilder streetBuilder = new StringBuilder();
                boolean isFirst = true;
                for (int i = 0; i < arrays.length - 3; i++) {
                    if (isFirst) {
                        isFirst = false;
                        streetBuilder.append(arrays[i]);
                    } else {
                        streetBuilder.append("-").append(arrays[i]);
                    }
                }
                return mapping2Object(streetBuilder.toString(), arrays[arrays.length - 3], arrays[arrays.length - 2], arrays[arrays.length - 1]);
        }
    }

    static AddressInfo mapping2Object(String streetName, String wardName, String districtName, String provinceName) {
        AddressInfo addressInfo = new AddressInfo();

        if (streetName != null) {
            if (WARD_PATTERN.matcher(streetName).matches()) {
                addressInfo.setWardName(streetName);
            } else {
                addressInfo.setStreetName(streetName);
            }
        }

        if (wardName != null) {
            if (WARD_PATTERN.matcher(wardName).matches()) {
                addressInfo.setWardName(wardName);
            } else {
                String street = addressInfo.getStreetName() != null ? addressInfo.getStreetName() + "-" + wardName : wardName;
                addressInfo.setStreetName(street);
            }
        }

        if (districtName != null) {
            if (DISTRICT_PATTERN.matcher(districtName).matches()) {
                addressInfo.setDistrictName(districtName);
            } else {
                String ward = addressInfo.getWardName() != null ? addressInfo.getWardName() + "-" + districtName : districtName;
                addressInfo.setWardName(ward);
            }
        }

        if (provinceName != null) {
            if (PROVINCE_PATTERN.matcher(provinceName).matches()) {
                addressInfo.setProvinceName(provinceName);
            } else {
                String district = addressInfo.getDistrictCode() != null ? addressInfo.getDistrictName() + "-" + provinceName : provinceName;
                addressInfo.setDistrictName(district);
            }
        }

        return addressInfo;
    }*/

}
