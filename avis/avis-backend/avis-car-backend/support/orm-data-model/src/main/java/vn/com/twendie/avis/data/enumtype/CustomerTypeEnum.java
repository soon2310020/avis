package vn.com.twendie.avis.data.enumtype;

public enum CustomerTypeEnum {

    INDIVIDUAL(1, "CT001", "Cá nhân"),
    ENTERPRISE(2, "CT002", "Doanh nghiệp");

    private Long value;

    private String code;

    private String name;

    CustomerTypeEnum(long value, String code, String name) {
        this.value = value;
        this.code = code;
        this.name = name;
    }

    public Long value() {
        return value;
    }

    public String code() {
        return code;
    }
}
