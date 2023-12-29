package vn.com.twendie.avis.data.enumtype;

public enum CommonStatusEnum {

    ENABLE(1), DISABLE(0);

    private int value;

    CommonStatusEnum(int value) {
        this.value = value;
    }
    public Integer value() {
        return value;
    }

}
