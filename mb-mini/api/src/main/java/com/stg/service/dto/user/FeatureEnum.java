package com.stg.service.dto.user;

import java.util.Arrays;
import java.util.List;

public enum FeatureEnum {
    MANAGEMENT_ALL("Quản Lý Tài Khoản, Phân Quyền"),
    MANAGEMENT_CAMPAIGN("Quản Lý Chiến Dịch"),
    MANAGEMENT_CUSTOMER("Quản Lý Khách Hàng"),
    MANAGEMENT_ILLUSTRATION("Quản Lý BMH"),
    MANAGEMENT_INSURANCE_PACKAGE("Quản Lý Gói Bảo Hiểm"),
    MANAGEMENT_DEPOSIT_PACKAGE("Quản Lý Gói Tiền Gửi"),
    MANAGEMENT_PAYMENT("Quản Lý Thanh Toán"),
    MANAGEMENT_YCBH("Quản Lý YCBH"),
    MANAGEMENT_INSURANCE_ISSUE("Quản Lý Phát Hành HĐBH");

    private String name;

    FeatureEnum(String name) {
        this.name = name;
    }

    public static List<FeatureEnum> getFeatureSuperAdmin() {
        return List.of(FeatureEnum.MANAGEMENT_ALL);
    }

    public static List<FeatureEnum> getFeatureAdmin() {
        return Arrays.asList(MANAGEMENT_CAMPAIGN, MANAGEMENT_CUSTOMER,
                MANAGEMENT_ILLUSTRATION, MANAGEMENT_INSURANCE_PACKAGE, MANAGEMENT_DEPOSIT_PACKAGE,
                MANAGEMENT_PAYMENT, MANAGEMENT_YCBH, MANAGEMENT_INSURANCE_ISSUE);
    }

}
