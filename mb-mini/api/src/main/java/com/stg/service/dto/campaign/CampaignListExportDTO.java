package com.stg.service.dto.campaign;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class CampaignListExportDTO {
    @CsvBindByName(column = "ID")
    @CsvBindByPosition(position = 0)
    public Long id;

    @CsvBindByName(column = "Tên chiến dịch")
    @CsvBindByPosition(position = 1)
    public String name;

    @CsvBindByName(column = "Sự kiện")
    @CsvBindByPosition(position = 2)
    public String event;

    @CsvBindByName(column = "Ngày bắt đầu")
    @CsvBindByPosition(position = 3)
    public String startTime;

    @CsvBindByName(column = "Ngày kết thúc")
    @CsvBindByPosition(position = 4)
    public String endTime;

    @CsvBindByName(column = "Trạng thái")
    @CsvBindByPosition(position = 5)
    public String status;

}
