package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum NotificationTypeEnum {

    CONTRACT("contract"), SIGNATURE("signature");

    private String value;

    public String value() {
        return value;
    }
}
