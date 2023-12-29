package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CorrectiveAction implements CodeMapperType {
    REQUEST("Request"),
    APPROVE("Approve"),
    DISAPPROVE("Disapprove"),
    COMPLETE("Complete");

    private String title;

    CorrectiveAction(String title) {
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
