package com.stg.service.dto.insurance;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceContractExportListDTO {

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 0)
    public String fullName;

    @CsvBindByName(column = "AW")
    @CsvBindByPosition(position = 1)
    public String mbTransactionId;

    @CsvBindByName(column = "Mã FT")
    @CsvBindByPosition(position = 2)
    public String mbFt;

    @CsvBindByName(column = "GCNBH")
    @CsvBindByPosition(position = 3)
    public String micContractNum;

    @CsvBindByName(column = "HĐBH")
    @CsvBindByPosition(position = 4)
    public String mbalPolicyNumber;

    @CsvBindByName(column = "Gói bảo hiểm")
    @CsvBindByPosition(position = 5)
    public String packageName;

    @CsvBindByName(column = "Thời gian đóng phí")
    @CsvBindByPosition(position = 6)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Phí bảo hiểm")
    @CsvBindByPosition(position = 7)
    public String strInsuranceFee;

    @CsvBindByName(column = "Phân khúc")
    @CsvBindByPosition(position = 8)
    public String segment;

}
