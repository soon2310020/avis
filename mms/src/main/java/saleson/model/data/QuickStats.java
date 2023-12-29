package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CurrencyType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickStats {
    Long partCount;
    Long moldCount;
    Integer cost;
    Long installedMoldPercentage;
    String symbol= CurrencyType.USD.getTitle();
}
