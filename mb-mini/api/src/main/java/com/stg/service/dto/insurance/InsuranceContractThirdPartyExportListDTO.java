package com.stg.service.dto.insurance;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractThirdPartyExportListDTO {

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 0)
    public String fullName;

    @CsvBindByName(column = "Số HSYCBH")
    @CsvBindByPosition(position = 1)
    public String mbalAppNo;

    @CsvBindByName(column = "Số HĐBH")
    @CsvBindByPosition(position = 2)
    public String mbalPolicyNumber;

    @CsvBindByName(column = "GCNBH")
    @CsvBindByPosition(position = 3)
    public String micContractNum;

    @CsvBindByName(column = "Tên sản phẩm")
    @CsvBindByPosition(position = 4)
    public String productName;

    @CsvBindByName(column = "Phí bảo hiểm")
    @CsvBindByPosition(position = 5)
    public String strInsuranceFee;

    @CsvBindByName(column = "Thời gian đóng phí")
    @CsvBindByPosition(position = 6)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Trạng thái")
    @CsvBindByPosition(position = 7)
    public String status;

    @CsvBindByName(column = "Nguồn truy vấn")
    @CsvBindByPosition(position = 8)
    public String source;

}
