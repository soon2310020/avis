package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentRequestEnum {

    RENTAL_PRICE(1L, "Giá thuê theo tháng (chưa VAT)", "Tháng"),
    OVERTIME_NORMAL_DAY(2L, "Over time (ngày thường)", "Giờ"),
    OVERTIME_SUNDAY(3L, "Over time (cuối tuần)", "Giờ"),
    OVERTIME_HOLIDAY(4L, "Over time (ngày lễ)", "Giờ"),
    OVER_KM_NORMAL_DAY(5L, "Over km (ngày thường)", "Km"),
    OVER_KM_CONTRACT_WITHOUT_DRIVER(5L, "Over km", "Km"),
    OVER_KM_SUNDAY(6L, "Over km (cuối tuần)", "Km"),
    OVER_KM_HOLIDAY(7L, "Over km (ngày lễ)", "Km"),
    OVER_KM_SELF_DRIVE(8L, "Over km (tự lái)", "Km"),
    SUNDAY_FEE_NORMAL(9L, "Sunday (có lái)", "Ngày"),
    SUNDAY_FEE_SELF_DRIVE(10L, "Sunday (tự lái)", "Ngày"),
    HOLIDAY_FEE_NORMAL(11L, "Holiday (có lái)", "Ngày"),
    HOLIDAY_FEE_SELF_DRIVE(12L, "Holiday (tự lái)", "Ngày"),
    NORMAL_DAY_FEE_SELF_DRIVE(13L, "Chi phí tự lái", "Ngày"),
    OVER_NIGHT_FEE(14L, "Overnight", "Đêm"),
    TOLL_FEE(15L, "Chi phí vé (đỗ xe + cầu đường)", "VNĐ"),
    OTHER_FEE(16L, "Chi phí khác", "VNĐ"),
    TOTAL_FEE_BEFORE_VAT(17L, "Tổng chi phí trước VAT", "VNĐ"),
    VAT_FEE(18L, "VAT (10%)", "VNĐ"),
    TOTAL_FEE(19L, "Tổng chi phí", "VNĐ");

    private Long id;
    private String name;
    private String unit;
}
