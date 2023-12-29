package vn.com.twendie.avis.api.mapping;

public class ContractVatTollFeeMapping implements ValueMapping<String> {

    private static final String BEFORE_TOLL_FEE = "Trước toll fee";
    private static final String AFTER_TOLL_FEE = "Sau toll fee";

    @Override
    public String map(Object value) {
        switch (Integer.parseInt(String.valueOf(value))) {
            case 0:
                return AFTER_TOLL_FEE;
            case 1:
                return BEFORE_TOLL_FEE;
            default:
                return null;
        }
    }
}
