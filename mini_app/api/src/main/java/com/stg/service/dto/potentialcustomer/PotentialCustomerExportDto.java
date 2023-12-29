package com.stg.service.dto.potentialcustomer;

import java.text.DecimalFormat;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.stg.utils.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PotentialCustomerExportDto {

    @CsvBindByName(column = "MB ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Tên KH")
    @CsvBindByPosition(position = 1)
    public String customerName;

    @CsvBindByName(column = "Phí bảo hiểm")
    @CsvBindByPosition(position = 2)
    public String amount;

    @CsvBindByName(column = "Thông tin RM")
    @CsvBindByPosition(position = 3)
    public String rmInfo;

    @CsvBindByName(column = "Thông tin IC")
    @CsvBindByPosition(position = 4)
    public String icInfo;

    @CsvBindByName(column = "Phân loại")
    @CsvBindByPosition(position = 5)
    public String classify;

    @CsvBindByName(column = "Ngày tham gia")
    @CsvBindByPosition(position = 6)
    public String createdDate;

    @CsvBindByName(column = "Ghi chú")
    @CsvBindByPosition(position = 7)
    public String note;

    @CsvBindByName(column = "Nguồn")
    @CsvBindByPosition(position = 8)
    public String source;

    public static PotentialCustomerExportDto of(SearchPotentialCustomerRes dto) {
        PotentialCustomerExportDto exportDto = new PotentialCustomerExportDto();
        exportDto.setMbId(dto.getCif());
        exportDto.setCustomerName(dto.getFullName());

        DecimalFormat df = new DecimalFormat("#,###");
        exportDto.setAmount(df.format(dto.getInputAmount()).replaceAll(",", ".") + " VNĐ");

        exportDto.setRmInfo(PotentialCustomerReferExportDto.buildRmInfo(dto.getRmInfo()));
        exportDto.setIcInfo(PotentialCustomerReferExportDto.buildIcInfo(dto.getIcInfo()));

        StringBuilder classify = new StringBuilder();
        if (dto.getTotalRefer() > 0) {
            classify.append(String.format("%d Refer", dto.getTotalRefer()));
        }
        if (dto.getTotalDirect() > 0) {
            if (classify.length() > 0) {
                classify.append("\n");
            }
            classify.append(String.format("%d Direct", dto.getTotalDirect())).append("\n");
        }
        exportDto.setClassify(classify.toString());

        exportDto.setNote(dto.getNote());
        exportDto.setCreatedDate(DateUtil.localDateTimeToString(dto.getCreatedDate(), DateUtil.DATE_DMY));
        exportDto.setSource("Tool");
        return exportDto;
    }

}
