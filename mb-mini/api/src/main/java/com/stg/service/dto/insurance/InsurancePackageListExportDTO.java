package com.stg.service.dto.insurance;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class InsurancePackageListExportDTO {
    @CsvBindByName(column = "ID")
    @CsvBindByPosition(position = 0)
    public Integer id;

    @CsvBindByName(column = "Tên gói")
    @CsvBindByPosition(position = 1)
    public String packageName;

    @CsvBindByName(column = "Thời gian đóng phí")
    @CsvBindByPosition(position = 2)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Tống phí bảo hiểm")
    @CsvBindByPosition(position = 3)
    public String totalFee;

    @CsvBindByName(column = "Tổng quyền lợi")
    @CsvBindByPosition(position = 4)
    public String totalBenefit;

    @CsvBindByName(column = "Đơn vị phát hành bảo hiểm")
    @CsvBindByPosition(position = 5)
    public String issuer;

}
