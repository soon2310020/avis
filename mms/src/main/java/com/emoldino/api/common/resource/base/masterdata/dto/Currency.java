package com.emoldino.api.common.resource.base.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CurrencyType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {
	private CurrencyType currencyType;
	private Double exchangeRate;
}
