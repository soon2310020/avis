package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum MatchStatus implements CodeMapperType {
    MATCHED("Matched"),
    UNMATCHED("Unmatched");

    private String title;

    MatchStatus(String title) {
        this.title = title;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return null;
    }
}
