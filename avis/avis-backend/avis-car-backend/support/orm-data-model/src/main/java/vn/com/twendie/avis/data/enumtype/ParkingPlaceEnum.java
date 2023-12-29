package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ParkingPlaceEnum {

    HOME(1L, "Khách Hàng"),
    AVIS(2L, "AVIS");

    private Long id;
    private String name;

    public static ParkingPlaceEnum valueOf(Long id) {
        return Arrays.stream(ParkingPlaceEnum.values())
                .filter(x -> x.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}
