package vn.com.twendie.avis.api.mapping;

import java.util.Objects;

public class UsingTimeUsePolicyMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return Objects.isNull(value) ? "Không"
                : Integer.parseInt(String.valueOf(value)) > 0 ? "Có" : "Không" ;
    }
}
