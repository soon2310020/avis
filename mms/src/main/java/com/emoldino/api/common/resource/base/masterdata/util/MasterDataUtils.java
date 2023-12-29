package com.emoldino.api.common.resource.base.masterdata.util;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.masterdata.dto.Currency;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.configuration.CurrencyConfigRepository;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CurrencyType;
import saleson.model.config.CurrencyConfig;
import saleson.model.config.QCurrencyConfig;

public class MasterDataUtils {

	public static Currency getMainCurrency() {
		QCurrencyConfig table = QCurrencyConfig.currencyConfig;

		CurrencyConfig config = null;

		String mainCurrencyCode = OptionUtils.getUserFieldValue(ConfigCategory.CURRENCY, "mainCurrencyCode", null);
		if (!ObjectUtils.isEmpty(mainCurrencyCode)) {
			CurrencyType currecyType = CurrencyType.valueOf(mainCurrencyCode);
			config = BeanUtils.get(CurrencyConfigRepository.class).findByCurrencyType(currecyType).orElse(null);
		}

		if (config == null) {
			for (CurrencyConfig item : BeanUtils.get(CurrencyConfigRepository.class).findAll(table.main.isTrue().and(table.deleted.isFalse()))) {
				config = item;
				break;
			}
		}

		if (config == null) {
			for (CurrencyConfig item : BeanUtils.get(CurrencyConfigRepository.class).findAll(table.currencyType.eq(CurrencyType.USD).and(table.deleted.isFalse()))) {
				config = item;
				break;
			}
		}

		if (config == null) {
			for (CurrencyConfig item : BeanUtils.get(CurrencyConfigRepository.class).findAll(table.deleted.isFalse())) {
				config = item;
				break;
			}
		}

		if (config == null) {
			for (CurrencyConfig item : BeanUtils.get(CurrencyConfigRepository.class).findAll(table.currencyType.eq(CurrencyType.USD))) {
				config = item;
				break;
			}
		}

		return config == null ? //
				Currency.builder().currencyType(CurrencyType.USD).exchangeRate(1d).build() //
				: Currency.builder().currencyType(config.getCurrencyType()).exchangeRate(config.getRate()).build();
	}

}
