package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;

public class ContractStatusMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return ContractStatusEnum.valueOf(Integer.parseInt(String.valueOf(value))).getName();
    }

}
