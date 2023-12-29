package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractVATEnum {

    VAT_BEFORE_TOLL_FEE(1, "Trước toll fee"),
    VAT_AFTER_TOLL_FEE(0, "Sau toll fee");

    private final Integer value;
    private final String name;
}
