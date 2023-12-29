package vn.com.twendie.avis.api.mapping;

import java.util.Objects;

public class BooleanToSymbolMapping implements ValueMapping<String> {

    private final String DISPLAY_SYMBOL;

    public BooleanToSymbolMapping(String display_symbol) {
        DISPLAY_SYMBOL = display_symbol;
    }

    @Override
    public String map(Object value) {
        if (!Objects.isNull(value) && (Boolean) value) {
            return DISPLAY_SYMBOL;
        } else {
            return "";
        }
    }

}
