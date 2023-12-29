package vn.com.twendie.avis.api.mapping;

import java.util.Objects;

public class OverTimeMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {

        if (Objects.isNull(value)) {
            return "";
        }

        int t = (Integer) value;

        int hours = t / 60;
        int minutes = t % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

}
