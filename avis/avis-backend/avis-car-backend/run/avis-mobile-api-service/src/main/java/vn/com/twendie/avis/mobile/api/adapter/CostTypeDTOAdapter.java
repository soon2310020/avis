package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.CostType;
import vn.com.twendie.avis.mobile.api.model.response.CostTypeDTO;

import java.util.function.Function;

public class CostTypeDTOAdapter implements Function<CostType, CostTypeDTO> {

    @Override
    public CostTypeDTO apply(CostType costType) {

        return CostTypeDTO.builder()
                .id(costType.getId())
                .code(costType.getCode())
                .name(costType.getName())
                .type(costType.getType())
                .build();

    }

}
