package com.stg.service3rd.mbal.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ProductType {
    TRMR("Bảo hiểm tử vong & thương tật toàn bộ vĩnh viễn"),
    ADDR("Bảo hiểm tai nạn"),
    HSCR("Bảo hiểm hỗ trợ viện phí & chi phí phẫu thuật"),
    CIR("Bảo hiểm bệnh hiểm nghèo"),
    PWR(" Bảo hiểm miễn thu phí bảo hiểm"),
    COI_RIDER("Bảo hiểm tử vong & thương tật toàn bộ vĩnh viễn do tai nạn");

    public final String label;

    ProductType(String label) {
        this.label = label;
    }

    public static String getProductTypeVal(String productType) {
        Optional<ProductType> optionalProductType = Arrays.stream(ProductType.values()).filter(ic ->
                ic.name().equals(productType)).findFirst();
        if (optionalProductType.isPresent()) {
            return optionalProductType.get().getLabel();
        }
        return "Not found";
    }
}
