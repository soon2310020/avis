package vn.com.twendie.avis.data.enumtype;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum VehicleStatusEnum implements Serializable {

    APPOINTED(2, "Đã điều động"),
    WAITING(1, "Chờ điều động"),
    UNAVAILABLE(0, "Không thể điều động");

    private final Integer value;
    private final String name;

    public static VehicleStatusEnum valueOf(Integer value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}
