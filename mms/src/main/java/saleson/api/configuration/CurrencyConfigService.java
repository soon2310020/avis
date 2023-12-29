package saleson.api.configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.masterdata.util.MasterDataUtils;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.framework.util.BeanUtils;

import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CurrencyType;
import saleson.dto.CurrencyConfigDTO;
import saleson.model.config.CurrencyConfig;
import saleson.model.config.QCurrencyConfig;
import saleson.service.rest.RestfulAPIService;

@Service
public class CurrencyConfigService {
	@Autowired
	private CurrencyConfigRepository repo;
	@Autowired
	private RestfulAPIService restfulAPIService;

	@Value("${api.exchange.url}")
	private String exchangeUrl;
	@Value("${api.exchange.accesskey}")
	private String accesskey;
	@Value("${exchange.period.allow.update}")
	private Integer periodUpdate;
	@Value("${exchange.currency.rate.basic}")
	private CurrencyType currencyTypeBasic;

	public List<CurrencyConfigDTO> get() {
		Map<CurrencyType, CurrencyConfig> configs = repo.findAllByDeletedIsFalseOrderByMainDesc()//
				.stream()//
				.collect(Collectors.toMap(CurrencyConfig::getCurrencyType, Function.identity()));
		List<CurrencyType> types = Arrays.asList(CurrencyType.values())//
				.stream()//
				.filter(currencyType -> configs.containsKey(currencyType))//
				.collect(Collectors.toList());
		CurrencyType mainCurrencyType = MasterDataUtils.getMainCurrency().getCurrencyType();
		if (types.contains(mainCurrencyType) && types.indexOf(mainCurrencyType) != 0) {
			types.remove(mainCurrencyType);
			types.add(0, mainCurrencyType);
		}
		List<CurrencyConfigDTO> list = new ArrayList<>(types.size());
		boolean first = true;
		for (CurrencyType type : types) {
			CurrencyConfigDTO item = CurrencyConfigDTO.convertToDTO(configs.get(type));
			item.setMain(first ? true : false);
			first = false;
			list.add(item);
		}
		return list;
	}

	public List<CurrencyConfig> getAllCurrencyConfigList() {
		return syncAndGetRate(false);
	}

	public CurrencyConfig setAsMain(CurrencyType currencyType) {
		BeanUtils.get(OptionService.class).saveUserFieldValue(ConfigCategory.CURRENCY, "mainCurrencyCode", currencyType.name());
		return postByCurrencyType(currencyType);
	}

	public CurrencyConfig postByCurrencyType(CurrencyType currencyType) {
		QCurrencyConfig table = QCurrencyConfig.currencyConfig;
		CurrencyConfig config = repo.findByCurrencyType(currencyType).orElse(null);
		if (config == null) {
			config = new CurrencyConfig(currencyType);
		}
		config.setDeleted(false);
		if (!repo.exists(table.deleted.isFalse().and(table.main.isTrue()))) {
			config.setMain(true);
		}
		repo.save(config);
		return config;
	}

	public void deleteByCurrencyType(CurrencyType currencyType) {
		CurrencyConfig currencyConfig = repo.findByCurrencyType(currencyType).orElse(null);
		if (currencyConfig == null) {
			return;
		}
		currencyConfig.setDeleted(true);
		repo.save(currencyConfig);
		if (currencyConfig.isMain()) {
			setAsMain(CurrencyType.USD);
		}
	}

	private List<CurrencyConfig> updateRate(List<CurrencyConfig> list) {
		try {
			Map<String, Object> stateParams = new HashMap<>();
			stateParams.put("access_key", accesskey);
			stateParams.put("format", 1);

			JSONObject response = restfulAPIService.send(exchangeUrl, stateParams, HttpMethod.GET, null);
			System.out.println("exchangeResponse:\n" + response.toString());
			if (response != null && response.has("success") && response.getBoolean("success") == true && response.has("rates")) {
				Double valueRateBasic = 1d;
//				ExchangeDTO restData = objectMapper.readValue(response.toString(), ExchangeDTO.class);
				Map<CurrencyType, Double> rateValueMap = new HashMap<>();
//				restData.setRateValueMap(rateValueMap);
				JSONObject rates = response.getJSONObject("rates");
				if (rates != null) {
//				if(restData.getRates()!=null)
					for (Iterator it = rates.keys(); it.hasNext();) {
						String key = (String) it.next();
						if (CurrencyType.asMyEnum(key) != null) {
							rateValueMap.put(CurrencyType.asMyEnum(key), rates.getDouble(key));
						}
					}
				}
				if (rateValueMap.containsKey(currencyTypeBasic))
					valueRateBasic = rateValueMap.get(currencyTypeBasic);
				for (CurrencyConfig currencyConfig : list) {
					if (rateValueMap.containsKey(currencyConfig.getCurrencyType())) {
						currencyConfig.setRate(rateValueMap.get(currencyConfig.getCurrencyType()) / valueRateBasic);
						currencyConfig.setUpdatedAt(Instant.now());
					}
				}
				repo.saveAll(list);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<CurrencyConfig> syncAndGetRate(boolean syncNow) {
		List<CurrencyConfig> listUpdate = repo.findAll();
		if (!syncNow && !listUpdate.isEmpty() && Instant.now().minus(periodUpdate, ChronoUnit.MINUTES).isBefore(listUpdate.get(0).getUpdatedAt())) {
			return listUpdate;
		}
		//init default
		List<CurrencyType> listTypeAll = listUpdate.stream().map(cf -> cf.getCurrencyType()).collect(Collectors.toList());
		for (CurrencyType currency : CurrencyType.values()) {
			if (currency.isEnabled() && !listTypeAll.contains(currency)) {
				CurrencyConfig currencyConfig = new CurrencyConfig(currency);
				currencyConfig.setDeleted(true);
				listUpdate.add(currencyConfig);
			}
		}

		return updateRate(listUpdate);
	}

//	public ExchangeDTO getRateByMain() {
//		ExchangeDTO exchangeDTO = new ExchangeDTO();
//		Map<CurrencyType, Double> mapRate = new HashMap<>();
//		exchangeDTO.setRateValueMap(mapRate);
//		List<CurrencyConfig> all = getAllCurrencyConfigList();
//		CurrencyConfig main = all.stream().filter(c -> c.isMain()).findFirst().orElse(null);
//		exchangeDTO.setBase(main != null ? main.getCurrencyType() : currencyTypeBasic);
////		CurrencyConfig currencyBasic =  all.stream().filter(currencyConfig -> currencyTypeBasic.equals(currencyConfig.getCurrencyType())).findFirst().orElse(null);
//		Double convertRate = main != null && main.getRate() != null ? main.getRate() : 1d;
//		all.stream().forEach(currencyConfig -> {
//			mapRate.put(currencyConfig.getCurrencyType(), currencyConfig.getRate() / convertRate);
//		});
//		return exchangeDTO;
//	}

}
