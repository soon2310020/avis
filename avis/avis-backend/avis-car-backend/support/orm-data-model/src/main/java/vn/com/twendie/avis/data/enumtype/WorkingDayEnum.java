package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum WorkingDayEnum {

    MON_TO_FRI(1L, "WD001", "Thứ 2 - Thứ 6"),
    MON_TO_SAT(2L, "WD002", "Thứ 2 - Thứ 7"),
    MON_TO_SUN(3L, "WD003", "Thứ 2 - Chủ nhật"),
    MON_TO_SAT_PLUS_1_SUN(4L, "WD004", "Thứ 2 - Thứ 7 kèm 1 ngày Chủ nhật"),
    MON_TO_SAT_PLUS_2_SUN(5L, "WD005", "Thứ 2 - Thứ 7 kèm 2 ngày Chủ nhật - 28 ngày"),
    FLEXIBLE(6L, "WD006", "Linh hoạt");

    private final Long id;
    private final String code;
    private final String name;

    public static WorkingDayEnum valueOf(Long id) {
        return Arrays.stream(WorkingDayEnum.values())
                .filter(x -> id.equals(x.getId()))
                .findFirst()
                .orElse(null);
    }
}
