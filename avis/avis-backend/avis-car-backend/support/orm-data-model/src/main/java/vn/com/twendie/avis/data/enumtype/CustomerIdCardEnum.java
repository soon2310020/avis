package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CustomerIdCardEnum {

    IDENTITY_CARD(1L, "identity_card", "Chứng minh nhân dân"),
    CITIZEN_IDENTIFICATION(2L, "citizen_identification", "Căn cước công dân"),
    PASSPORT(3L, "passport", "Hộ chiếu");

    private Long id;
    private String code;
    private String name;

    public static CustomerIdCardEnum valueOf(Long id) {
        return Arrays.stream(CustomerIdCardEnum.values())
                .filter(x -> x.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}
