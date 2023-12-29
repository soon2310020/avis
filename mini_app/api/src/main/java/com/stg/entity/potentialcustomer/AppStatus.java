package com.stg.entity.potentialcustomer;

public enum AppStatus {
    DRAFT("Nháp"),
    SUBMITTED("Nộp thánh công"),
    PENDING("Chờ phát hành"),
    COMPLETED("Phát hành thành công"),
    REJECTED("Từ chối");
    
    
    private String text;
    
    private AppStatus(String text) {
        this.text = text;
    }
    
    public String text() {
        return text;
    }

}
