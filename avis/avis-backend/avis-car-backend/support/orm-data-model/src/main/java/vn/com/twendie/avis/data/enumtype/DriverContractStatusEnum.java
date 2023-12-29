package vn.com.twendie.avis.data.enumtype;

public enum DriverContractStatusEnum {

    POSTPONE(0, "postpone", "Tạm dừng"),
    CONTINUE(1, "continue", "Tiếp tục");

    private int code;

    private String alias;

    private String name;

    DriverContractStatusEnum(int code, String alias, String name) {
        this.code = code;
        this.alias = alias;
        this.name = name;
    }

    public Integer code() {
        return code;
    }

    public String alias() {
        return alias;
    }
}
