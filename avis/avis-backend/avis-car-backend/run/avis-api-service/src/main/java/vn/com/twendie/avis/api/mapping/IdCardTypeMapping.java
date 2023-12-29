package vn.com.twendie.avis.api.mapping;

import org.apache.commons.lang3.StringUtils;
import vn.com.twendie.avis.data.enumtype.CustomerIdCardEnum;

import java.util.Objects;

public class IdCardTypeMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        CustomerIdCardEnum cardEnum = CustomerIdCardEnum.valueOf(Long.parseLong(String.valueOf(value)));
        return Objects.isNull(cardEnum) ? "" : cardEnum.getName();
    }

}
