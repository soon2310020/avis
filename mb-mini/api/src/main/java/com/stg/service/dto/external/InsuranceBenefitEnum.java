package com.stg.service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InsuranceBenefitEnum {
    MAIN_I ("Tử vong/thương tật vĩnh viễn do tai nạn"),
    MAIN_II("Chi phí y tế do tai nạn"),

    MAIN_III("Điều trị nội trú do ốm đau, bệnh tật"),
    MAIN_III_1("Nằm viện do ốm đau, bệnh tật (tối đa 60 ngày/năm)"),
    MAIN_III_2("Nằm viện do ốm đau, bệnh tật (tối đa 60 ngày/năm)"),
    MAIN_III_3("Điều trị trước/ sau khi nhập viện hoặc y tế chăm sóc tại nhà sau xuất viện"),
    MAIN_III_4("Trợ cấp nằm viện/ngày (tối đa 60 ngày)"),
    MAIN_III_5("Dịch vụ xe cứu thương "),
    MAIN_III_6("Phục hồi chức năng"),
    MAIN_III_7("Trợ cấp mai táng phí"),

    SUB_I("ĐKBS 01 - Ngoại trú do ốm đau, bệnh tật"),
    SUB_I_1("Chi phí khám ngoại trú"),
    SUB_I_2("Vật lý trị liệu"),

    SUB_II("ĐKBS 02 - Quyền lợi nha khoa"),
    SUB_III("ĐKBS 03 - Quyền lợi thai sản"),
    SUB_IV("ĐKBS 04 - Tử vong, thương tật toàn bộ vĩnh viễn do ốm đau, bệnh tật");

    private String val;
}
