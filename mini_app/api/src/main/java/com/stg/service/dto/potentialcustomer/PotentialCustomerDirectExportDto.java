package com.stg.service.dto.potentialcustomer;

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
public class PotentialCustomerDirectExportDto {

    @CsvBindByName(column = "MB ID")
    @CsvBindByPosition(position = 0)
    public String mbalId;

    @CsvBindByName(column = "Lead ID")
    @CsvBindByPosition(position = 1)
    public String leadId;

    @CsvBindByName(column = "Tên KH")
    @CsvBindByPosition(position = 2)
    public String customerName;

    @CsvBindByName(column = "Thông tin RM")
    @CsvBindByPosition(position = 3)
    public String rmInfo;

    @CsvBindByName(column = "Thông tin IC")
    @CsvBindByPosition(position = 4)
    public String icInfo;

    @CsvBindByName(column = "GTTT")
    @CsvBindByPosition(position = 5)
    public String identificationId;

    @CsvBindByName(column = "Trạng thái HSYCBH")
    @CsvBindByPosition(position = 6)
    public String appStatus;

    @CsvBindByName(column = "Ngày tham gia")
    @CsvBindByPosition(position = 7)
    public String createdDate;

    @CsvBindByName(column = "Nguồn")
    @CsvBindByPosition(position = 8)
    public String source;

    public static PotentialCustomerDirectExportDto of(SearchDirectPotentialCustomerRes dto) {
        PotentialCustomerDirectExportDto exportDto = new PotentialCustomerDirectExportDto();
        exportDto.setMbalId(dto.getCif());
        exportDto.setLeadId(dto.getLeadId());
        exportDto.setCustomerName(dto.getFullName());
        exportDto.setRmInfo(PotentialCustomerReferExportDto.buildRmInfo(dto.getRmInfo()));
        exportDto.setIcInfo(PotentialCustomerReferExportDto.buildIcInfo(dto.getIcInfo()));
        exportDto.setIdentificationId(dto.getIdentificationId());
        exportDto.setAppStatus(dto.getAppStatus() != null ? dto.getAppStatus().text() : null);
        exportDto.setCreatedDate(DateUtil.localDateTimeToString(dto.getCreatedDate(), DateUtil.DATE_DMY));
        exportDto.setSource("Tool");
        return exportDto;
    }

}
