package saleson.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CurrencyType;
import saleson.common.util.DataUtils;
import saleson.model.config.CurrencyConfig;

@Data
@NoArgsConstructor
public class CurrencyConfigDTO {
    private Long id;
    private CurrencyType currencyType;
    private String name;
    private String symbol;
    private Double rate;
    private boolean main;

    public static CurrencyConfigDTO convertToDTO(CurrencyConfig currencyConfig) {
        CurrencyConfigDTO dto = DataUtils.mapper.map(currencyConfig,CurrencyConfigDTO.class);
        if(currencyConfig.getCurrencyType()!=null){
            dto.setName(currencyConfig.getCurrencyType().getDescription());
            dto.setSymbol(currencyConfig.getCurrencyType().getTitle());
        }
        return dto;
    }
}
