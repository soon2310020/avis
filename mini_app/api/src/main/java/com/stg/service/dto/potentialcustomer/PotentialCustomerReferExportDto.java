package com.stg.service.dto.potentialcustomer;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.utils.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PotentialCustomerReferExportDto {

    @CsvBindByName(column = "Lead ID")
    @CsvBindByPosition(position = 0)
    public String leadId;

    @CsvBindByName(column = "Tên KH")
    @CsvBindByPosition(position = 1)
    public String customerName;

    @CsvBindByName(column = "Thông tin RM")
    @CsvBindByPosition(position = 2)
    public String rmInfo;

    @CsvBindByName(column = "Thông tin IC")
    @CsvBindByPosition(position = 3)
    public String icInfo;

    @CsvBindByName(column = "GTTT")
    @CsvBindByPosition(position = 4)
    public String identificationId;

    @CsvBindByName(column = "Trạng thái HSYCBH")
    @CsvBindByPosition(position = 5)
    public String appStatus;

    @CsvBindByName(column = "Trạng thái Lead")
    @CsvBindByPosition(position = 6)
    public String leadStatus;

    @CsvBindByName(column = "Ngày tham gia")
    @CsvBindByPosition(position = 7)
    public String createdDate;

    @CsvBindByName(column = "Nguồn")
    @CsvBindByPosition(position = 8)
    public String source;

    public static PotentialCustomerReferExportDto of(PotentialCustomerReferredHeaderDto dto) {
        PotentialCustomerReferExportDto exportDto = new PotentialCustomerReferExportDto();
        exportDto.setLeadId(dto.getLeadId());
        exportDto.setCustomerName(dto.getPotentialCustomerFullName());
        exportDto.setRmInfo(buildRmInfo(dto.getRmInfo()));
        exportDto.setIcInfo(buildIcInfo(dto.getIcInfo()));
        exportDto.setIdentificationId(dto.getPotentialCustomerIdentificationId());
        exportDto.setAppStatus(dto.getAppStatus() != null ? dto.getAppStatus().text() : null);
        exportDto.setLeadStatus(dto.getLeadStatus());
        exportDto.setCreatedDate(DateUtil.localDateTimeToString(dto.getCreatedDate(), DateUtil.DATE_DMY));
        exportDto.setSource("Tool");
        return exportDto;
    }

    public static String buildRmInfo(FlexibleCommon.ReferrerInput info) {
        StringBuilder builder = new StringBuilder();

        if (info != null) {
            builder.append("Mã MB: ").append(info.getCode()).append("\n");
            builder.append("Tên: ").append(info.getName()).append("\n");
            builder.append("Mã chi nhánh: ").append(info.getBranchCode()).append("\n");
            builder.append("Tên chi nhánh: ").append(info.getBranchName());
        }

        return builder.toString();
    }

    public static String buildIcInfo(FlexibleCommon.ReferrerInput info) {
        StringBuilder builder = new StringBuilder();

        if (info != null) {
            builder.append("Mã IC: ").append(info.getCode()).append("\n");
            builder.append("Tên: ").append(info.getName()).append("\n");
            builder.append("Mã chi nhánh: ").append(info.getBranchCode()).append("\n");
            builder.append("Tên chi nhánh: ").append(info.getBranchName());
        }

        return builder.toString();
    }
}
