package vn.com.twendie.avis.data.enumtype;

public enum ContractTypeEnum {

    WITH_DRIVER(1L, "Hợp đồng có lái xe"),
    WITHOUT_DRIVER(2L, "Hợp đồng không lái xe");

    private final Long value;

    private final String name;

    ContractTypeEnum(Long value, String name) {
        this.value = value;
        this.name = name;
    }

    public Long value() {
        return value;
    }

    public static ContractTypeEnum valueOf(Long value) {
        for (ContractTypeEnum contractTypeEnum : values()) {
            if (contractTypeEnum.value().equals(value)) {
                return contractTypeEnum;
            }
        }
        return null;
    }
}
