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
public class InsurancePaymentExportListDTO {
    @CsvBindByName(column = "MB ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Thời gian")
    @CsvBindByPosition(position = 2)
    public String paymentTime;

    @CsvBindByName(column = "Tổng phí BH")
    @CsvBindByPosition(position = 3)
    public String totalInsuranceFee;

    @CsvBindByName(column = "Trạng thái thanh toán")
    @CsvBindByPosition(position = 4)
    public String tranStatus;

    @CsvBindByName(column = "Mã giao dịch")
    @CsvBindByPosition(position = 5)
    public String mbTransactionId;

    @CsvBindByName(column = "MB FT")
    @CsvBindByPosition(position = 6)
    public String mbFt;

    @CsvBindByName(column = "MIC FT")
    @CsvBindByPosition(position = 7)
    public String micFt;

    @CsvBindByName(column = "GCNBH")
    @CsvBindByPosition(position = 8)
    public String micContractNum;

    @CsvBindByName(column = "Phí BH MIC")
    @CsvBindByPosition(position = 9)
    public String micInsuranceFee;

    @CsvBindByName(column = "Chi hộ MIC")
    @CsvBindByPosition(position = 10)
    public String baasMicStatus;

    @CsvBindByName(column = "MBAL FT")
    @CsvBindByPosition(position = 11)
    public String mbalFt;

    @CsvBindByName(column = "Số HSYCBH")
    @CsvBindByPosition(position = 12)
    public String mbalAppNo;

    @CsvBindByName(column = "Phí BH MBAL")
    @CsvBindByPosition(position = 13)
    public String mbalInsuranceFee;

    @CsvBindByName(column = "Chi hộ MBAL")
    @CsvBindByPosition(position = 14)
    public String baasMbalStatus;

    @CsvBindByName(column = "Thời gian đóng phí")
    @CsvBindByPosition(position = 15)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Trạng thái đối soát")
    @CsvBindByPosition(position = 16)
    public String controlState;

    @CsvBindByName(column = "LLBH (RM)")
    @CsvBindByPosition(position = 17)
    public String presenter;

    @CsvBindByName(column = "Tư vấn viên (IC)")
    @CsvBindByPosition(position = 18)
    public String icStaff;

    @CsvBindByName(column = "Người hỗ trợ")
    @CsvBindByPosition(position = 19)
    public String supporter;

    @CsvBindByName(column = "Chi nhánh (RM)")
    @CsvBindByPosition(position = 20)
    public String branch;

    @CsvBindByName(column = "Phòng giao dịch")
    @CsvBindByPosition(position = 21)
    public String department;

    public void setPaymentTime(LocalDateTime dateTime) {
        this.paymentTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY_HMS, dateTime);
    }
}
