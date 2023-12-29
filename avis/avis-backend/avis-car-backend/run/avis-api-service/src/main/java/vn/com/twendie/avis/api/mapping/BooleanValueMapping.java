package vn.com.twendie.avis.api.mapping;

public class BooleanValueMapping implements ValueMapping<String> {

    private static final String YES = "Có";
    private static final String NO = "Không";

    @Override
    public String map(Object value) {
        return Boolean.parseBoolean(String.valueOf(value)) ? YES : NO;
    }
}
