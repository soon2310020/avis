package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationContentEnum {

    // Driver
    NEW_CONTRACT(1L, "new_contract", "Tài xế được nhận HĐ mới"),
    CHANGE_VEHICLE(2L, "change_vehicle", "Thông báo đổi xe"),
    CHANGE_CONTRACT_DURATION(3L, "change_contract_duration", "Thông báo thay đổi thời hạn HĐ"),
    CANCELED_CONTRACT(4L, "canceled_contract", "Thông báo hủy HĐ / Kết thúc sớm HĐ"),
    PAUSE_CONTRACT(5L, "pause_contract", "Thông báo tạm dừng hợp đồng"),
    CONTRACT_CHANGE(6L, "contract_change", "Thông báo về thay đổi user/sdt (hợp đồng)"),

    // Customer
    NEW_CONTRACT_CUSTOMER(7L, "new_contract_customer", "Thông báo thêm mới HĐ"),
    CHANGE_CONTRACT_CUSTOMER(8L, "change_contract_customer", "Thông báo thay đổi HĐ)"),
    FINISH_CONTRACT_CUSTOMER(9L, "finish_contract_customer", "Thông báo hoàn thành HĐ"),
    CANCEL_CONTRACT_CUSTOMER(10L, "cancel_contract_customer", "Thông báo hủy HĐ"),
    DELETE_CONTRACT_CUSTOMER(11L, "delete_contract_customer", "Thông báo xóa HĐ"),

//    Signature
    END_JOURNEY_DIARY_SIGNATURE_DAY(12L,"end_journey_diary_day", "Thông báo ký xác nhận kết thúc hành trình"),
    END_JOURNEY_DIARY_SIGNATURE_WEEK(13L, "end_journey_diary_week", "Thông báo ký xác nhận kết thúc hành trình tuần"),
    END_JOURNEY_DIARY_SIGNATURE_MONTH(14L, "end_journey_diary_month", "Thông báo ký xác nhận kết thúc hành trình tuần");

    private final Long id;

    private final String alias;

    private final String name;

    public static NotificationContentEnum findById(long id) {
        for (NotificationContentEnum item : NotificationContentEnum.values()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
