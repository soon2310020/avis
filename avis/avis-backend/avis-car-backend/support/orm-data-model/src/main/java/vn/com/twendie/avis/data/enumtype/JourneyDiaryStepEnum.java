package vn.com.twendie.avis.data.enumtype;


public enum JourneyDiaryStepEnum {

    STARTED(1), CUSTOMER_GOT_IN(2), CUSTOMER_GOT_OUT(3), FINISHED(4), BREAKDOWN(5);

    private final Integer value;

    JourneyDiaryStepEnum(int value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }
}