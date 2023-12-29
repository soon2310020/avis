package com.stg.utils.address;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtil {
    private static final Pattern OCR_ADDRESS_PATTERN = Pattern.compile( //NOSONAR
            "\\s*(,|TỈNH |THÀNH PHỐ |HUYỆN |THỊ XÃ |QUẬN |XÃ |THỊ TRẤN |PHƯỜNG |TT |TX |TP |Q )\\s*",
            CASE_INSENSITIVE | CANON_EQ | UNICODE_CHARACTER_CLASS);

    /**
     * SubAddressUnknown != null <=> addressSeperated.length < 3
     */
    public static AddressInfo splitOCRAddress(String ocrAddress) {
        String[] addressSeperated = Arrays.stream(OCR_ADDRESS_PATTERN.split(ocrAddress)).filter(el -> !el.isEmpty()).toArray(String[]::new);
        AddressInfo addressInfo = new AddressInfo();

        if (addressSeperated.length > 4) {
            StringBuilder streetBuilder = new StringBuilder();
            boolean isFirst = true;
            for (int i = 0; i < addressSeperated.length - 3; i++) {
                if (isFirst) {
                    isFirst = false;
                    streetBuilder.append(addressSeperated[i]);
                } else {
                    streetBuilder.append("-").append(addressSeperated[i]);
                }
            }

            addressInfo.setStreetName(streetBuilder.toString());
            addressInfo.setProvinceName(addressSeperated[addressSeperated.length - 1]);
            addressInfo.setDistrictName(addressSeperated[addressSeperated.length - 2]);
            addressInfo.setWardName(addressSeperated[addressSeperated.length - 3]);
        } else if (addressSeperated.length == 4) {
            addressInfo.setProvinceName(addressSeperated[addressSeperated.length - 1]);
            addressInfo.setDistrictName(addressSeperated[addressSeperated.length - 2]);
            addressInfo.setWardName(addressSeperated[addressSeperated.length - 3]);
            addressInfo.setStreetName(addressSeperated[addressSeperated.length - 4]);
        } else if (addressSeperated.length == 3) {
            addressInfo.setProvinceName(addressSeperated[addressSeperated.length - 1]);
            addressInfo.setDistrictName(addressSeperated[addressSeperated.length - 2]);
            addressInfo.setWardName(addressSeperated[addressSeperated.length - 3]);
        } else {
            // addressSeperated.length <= 2
            addressInfo.setSubAddressUnknown(StringUtils.join(addressSeperated, "-"));
        }

        return addressInfo;
    }

    public static void main(String[] args) {
        String[] address = new String[]{
                "Xã Nghĩa Trung Huyện Việt Yên, Bắc Giang",
                "Xã Tân Dân Huyện Tĩnh Gia Tỉnh Thanh Hóa",
                "Hòa Tân, xã Nghĩa Hòa, huyện Tu Nghĩa, tỉnh Quảng Ngãi"
        };
        for (String addr : address) {
            System.out.println(splitOCRAddress(addr));
        }
    }
}
