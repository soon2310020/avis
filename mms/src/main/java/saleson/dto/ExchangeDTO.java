package saleson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CurrencyType;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeDTO {
    boolean success;
    Instant timestamp;
    CurrencyType base;
    JsonObject rates;
    Map<CurrencyType,Double> rateValueMap;
}
