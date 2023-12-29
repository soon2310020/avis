package com.stg.utils;

import lombok.Getter;

@Getter
public enum InstallmentMessageType {
    TG_01, // KH chọn thanh toán bằng Tài khoản thanh toán
    TG_02, // KH đăng ký trả góp không thành công
    TG_03; // KH đăng ký trả góp thành công
}
