package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum VehicleTransmissionTypeEnum implements Serializable {

    AUTO(2, "AT"),
    MANUAL(1, "MT");

    private final Integer value;
    private final String name;

    public static VehicleTransmissionTypeEnum valueOf(Integer value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}
