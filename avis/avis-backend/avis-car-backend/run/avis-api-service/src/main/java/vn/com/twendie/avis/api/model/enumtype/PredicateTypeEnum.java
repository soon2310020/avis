package vn.com.twendie.avis.api.model.enumtype;

public enum PredicateTypeEnum {

    EQ("Eq"), IN("In"), LIKE("Like"), NOT("Not"), GT("Gt"), GTE("Gte"), LT("Lt"), LTE("Lte");

    private final String alias;

    PredicateTypeEnum(String alias) {
        this.alias = alias;
    }

    public static PredicateTypeEnum parse(String key, Object value) {
        for (PredicateTypeEnum predicateType : values()) {
            if (key.endsWith(predicateType.alias)) {
                return predicateType;
            }
        }
        if (String.valueOf(value).contains("%")) {
            return LIKE;
        } else {
            return EQ;
        }
    }

    public String alias() {
        return alias;
    }

}
