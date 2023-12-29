package vn.com.twendie.avis.data.enumtype;

public enum NotificationStatusEnum {

    WAITING("waiting"), SUCCESS("success"), FAILED("failed"), READ("read");

    private String value;

    NotificationStatusEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}
