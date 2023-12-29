package vn.com.twendie.avis.data.enumtype;

import lombok.Getter;

@Getter
public enum ContractPeriodTypeEnum {

    LONG_TERM(1L, "Dài hạn"), SHORT_TERM(2L, "Ngắn hạn");

    private final Long id;

    private final String name;

    ContractPeriodTypeEnum(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
