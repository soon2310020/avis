package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum Language implements CodeMapperType {
    EN("English"),
    DE("German"),
    ZH("Chinese"),
    PT("Portuguese"),
    FR("French"),
    IT("Italian"),
    ES("Spanish"),
    JA("Japanese"),
    TR("Turkish"),
    KO("Korean");

    private String title;

    Language(String title) {
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