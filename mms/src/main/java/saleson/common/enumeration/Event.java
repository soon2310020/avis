package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum Event implements CodeMapperType {
    RECONNECT("Reconnect"),
    DISCONNECT("Disconnect");

    private String title;

    Event(String title) {
        this.title = title;
    }


    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public Boolean isEnabled() {
        return true;
    }
}
