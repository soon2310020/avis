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
public class InsurancePaymentWaitingExportDto {

    @CsvBindByName(column = "MB ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Loại giấy tờ")
    @CsvBindByPosition(position = 2)
    public String idCardType;

    @CsvBindByName(column = "Số giấy tờ")
    @CsvBindByPosition(position = 3)
    public String idCardNo;

    @CsvBindByName(column = "Thời gian")
    @CsvBindByPosition(position = 4)
    public String paymentTime;

    @CsvBindByName(column = "Tổng phí BH")
    @CsvBindByPosition(position = 5)
    public String totalInsuranceFee;

    @CsvBindByName(column = "Mã giao dịch")
    @CsvBindByPosition(position = 6)
    public String mbTransactionId;

    @CsvBindByName(column = "Phí BH MIC")
    @CsvBindByPosition(position = 7)
    public String micInsuranceFee;

    @CsvBindByName(column = "Số HSYCBH")
    @CsvBindByPosition(position = 8)
    public String mbalAppNo;

    @CsvBindByName(column = "Phí BH MBAL")
    @CsvBindByPosition(position = 9)
    public String mbalInsuranceFee;

    @CsvBindByName(column = "Trạng thái thanh toán")
    @CsvBindByPosition(position = 10)
    public String tranStatus;

    public void setPaymentTime(LocalDateTime dateTime) {
        this.paymentTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, dateTime);
    }
}
