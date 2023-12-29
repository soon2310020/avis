package vn.com.twendie.avis.api.mapping;

import java.math.BigDecimal;
import java.util.Objects;

public class CountValueToSymbolMapping implements ValueMapping<String> {

    private final String DISPLAY_SYMBOL;

    public CountValueToSymbolMapping(String display_symbol) {
        DISPLAY_SYMBOL = display_symbol;
    }

    @Override
    public String map(Object value) {
        if (Objects.nonNull(value)) {
            BigDecimal count = (BigDecimal) value;
            return count.compareTo(BigDecimal.ZERO) > 0 ? String.valueOf(value) : DISPLAY_SYMBOL;
        } else {
            return null;
        }
    }

}
