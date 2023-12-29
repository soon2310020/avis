package com.stg.service.dto.customer;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class CustomerListExportDTO {
    @CsvBindByName(column = "MB_ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Tên KH sửa")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Tên KH trên T24")
    @CsvBindByPosition(position = 2)
    public String fullNameT24;
    
    @CsvBindByName(column = "Thời gian cập nhật")
    @CsvBindByPosition(position = 3)
    public String lastUpdated;
    
    @CsvBindByName(column = "Email")
    @CsvBindByPosition(position = 4)
    public String email;

    @CsvBindByName(column = "Ngày sinh")
    @CsvBindByPosition(position = 5)
    public String birthDay;

    @CsvBindByName(column = "Phân khúc")
    @CsvBindByPosition(position = 6)
    public String segment;

    @CsvBindByName(column = "Nguồn truy vấn")
    @CsvBindByPosition(position = 7)
    public String source;

}
