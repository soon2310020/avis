package com.stg.service.dto.customer;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class CustomerDetailExportDTO {
    @CsvBindByName(column = "MB_ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Ngày sinh")
    @CsvBindByPosition(position = 2)
    public String birthDay;

    @CsvBindByName(column = "Giới tính")
    @CsvBindByPosition(position = 3)
    public String gender;

    @CsvBindByName(column = "Quốc tịch")
    @CsvBindByPosition(position = 4)
    public String nationality;

    @CsvBindByName(column = "Số điện thoại")
    @CsvBindByPosition(position = 5)
    public String phone;

    @CsvBindByName(column = "Nghề nghiệp")
    @CsvBindByPosition(position = 6)
    public String job;

    @CsvBindByName(column = "Loại giấy tờ tùy thân")
    @CsvBindByPosition(position = 7)
    public String idCardType;

    @CsvBindByName(column = "Số giấy tờ tùy thân")
    @CsvBindByPosition(position = 8)
    public String identification;

    @CsvBindByName(column = "Email")
    @CsvBindByPosition(position = 9)
    public String email;

    @CsvBindByName(column = "Địa chỉ")
    @CsvBindByPosition(position = 10)
    public String address;

    @CsvBindByName(column = "Tên gói bảo hiểm")
    @CsvBindByPosition(position = 11)
    public String nameInsurance;

    @CsvBindByName(column = "Số BMH")
    @CsvBindByPosition(position = 12)
    public String illustrationNumber;

    @CsvBindByName(column = "Số GCNBH")
    @CsvBindByPosition(position = 13)
    public String micContractNum;

    @CsvBindByName(column = "Số HSYCBH")
    @CsvBindByPosition(position = 14)
    public String mbalAppNo;

    @CsvBindByName(column = "Thời hạn")
    @CsvBindByPosition(position = 15)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Giá trị hợp đồng")
    @CsvBindByPosition(position = 16)
    public String contractValue;

    @CsvBindByName(column = "Đơn vị phát hành")
    @CsvBindByPosition(position = 17)
    public String issuer;

    @CsvBindByName(column = "Ngày khởi tạo GCNBH")
    @CsvBindByPosition(position = 18)
    public String releaseDateMic;

    @CsvBindByName(column = "Ngày khởi tạo HSYCBH")
    @CsvBindByPosition(position = 19)
    public String releaseDateMbal;
}
