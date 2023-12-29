package com.emoldino.api.common.resource.base.client.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.MapBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ClientUtils {
	public static final String CENTRAL = "central";
	public static final String DEV = "dev";
	public static final String FEATURE = "feature";
	public static final String PRODUCTION = "production";
	public static final String DEMO = "demo";
	public static final String DAYOU_PLUS = "dayouplus";
	public static final String DAYOU_EP = "dayouep";
	public static final String ERAE_CS = "eraecs";

	public static final String AAM = "aam";
	public static final String ABB_CHINA = "abb-china";
	public static final String ABB_GERMANY = "abb-pilot";
	public static final String ADIENT = "adient-0422";
	public static final String BEIERSDORF = "beiesdoft";
	public static final String BAYLIS_MEDICAL = "bc0822";
	public static final String BOTICARIO = "bb0703";
	public static final String CEPHEID = "cepheid";
	public static final String CONTINENTAL = "cb0413";
	public static final String CUMMINS = "cummins";
	public static final String DAIKIN = "dn0525";
	public static final String DELONGHI = "di0402";
	public static final String DENSO_INDIA = "di0711";
	public static final String DENSO_ITALY = "fc0616";
	public static final String DYSON = "dyson";
	public static final String EATON = "eaton";
	public static final String ELECTROLUX_BRAZIL = "eb0702";
	public static final String FLEX = "flex";
	public static final String HOFMANN_PLASTICS = "hofmannplastics";
	public static final String HONEYWELL = "honeywell";
	public static final String IROBOT = "ic0822";
	public static final String IVECO_GROUP = "ivecogroup";
	public static final String JABIL = "ju0728";
	public static final String JEILMETAL = "jeilmetal";
	public static final String JAGUAR = "ju0316";
	public static final String LOREAL = "lf0408";
	public static final String MABE = "mm0427";
	public static final String NESTLE = "ns0407";
	public static final String NICE = "nice";
	public static final String NISSAN = "nissan";
	public static final String PACCAR = "paccar";
	public static final String PERFETTI_VANMELLE = "perfettivanmelle";
	public static final String PG = "p&g";
	public static final String PHILIPS = "philips";
	public static final String PLASTICOMNIUM = "plasticomnium";
	public static final String RENAULT_TURKEY = "renaulttr";
	public static final String RIL = "ril";
	public static final String SCHAEFFLER = "schaeffler";
	public static final String TESLA = "tesla";
	public static final String VESTEL = "vestel";
	public static final String VIBRACOUSTIC = "vibracoustic";
	public static final String VOLVO = "volvo";
	public static final String ZEBRA = "zebra";
	public static final String ZF = "zf";

	public static final String ELECTROLUX_US = "en-usa";
	// FORD
	// ICEE
	// MISUMI
	public static final String STANLEY_BD = "stanley";

	private static final Map<String, ClientInfo> CLIENT = new MapBuilder<String, ClientInfo>()//
			.put(CENTRAL, new ClientInfo("Central", true, false, true, "https://central.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//

			.put(DEV, new ClientInfo("Dev", true, false, true, "https://dev.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(FEATURE, new ClientInfo("Feature", true, false, true, "https://feature.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(PRODUCTION, new ClientInfo("Production", true, true, true, "https://production.emoldino.com", "/images/common/logo/production-logo.svg"))//
			.put(DEMO, new ClientInfo("Demo", true, true, true, "https://demo.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(DAYOU_PLUS, new ClientInfo("Dayou Plus", true, true, true, "https://dayouplus.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(DAYOU_EP, new ClientInfo("Dayou EP", true, true, true, "https://dayouep.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(ERAE_CS, new ClientInfo("Erae", true, true, true, "https://erae.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//

			.put(AAM, new ClientInfo("AAM", true, true, "https://aam.emoldino.com", "/images/common/logo/aam-logo.svg"))//
			.put(ABB_CHINA, new ClientInfo("abbchn", "ABB China", true, true, "https://abbchn.emoldino.com", "/images/common/logo/abb-logo.svg"))//
			.put(ABB_GERMANY, new ClientInfo("abb", "ABB Germany", true, true, "https://abb.emoldino.com", "/images/common/logo/abb-logo.svg"))//
			.put(ADIENT, new ClientInfo("adient", "Adient", true, true, "https://adient.emoldino.com", "/images/common/logo/adient-logo.svg"))//
			.put(BEIERSDORF, new ClientInfo("Beiersdorf", true, true, "https://beiersdorf.emoldino.com", "/images/common/logo/beiersdorf-logo.svg"))//
			.put(BAYLIS_MEDICAL, new ClientInfo("baylis", "Baylis Medical", true, true, "https://baylis.emoldino.com", "/images/common/logo/boston-logo.svg"))//
			.put(BOTICARIO, new ClientInfo("boticario", "Boticario", true, true, "https://boticario.emoldino.com", "/images/common/logo/boticario-logo.svg"))//
			.put(CEPHEID, new ClientInfo("Cepheid", true, true, "https://cepheid.emoldino.com", "/images/common/logo/cepheid-logo.svg"))//
			.put(CONTINENTAL, new ClientInfo("continental", "Continental", true, true, "https://continental.emoldino.com", "/images/common/logo/continental-logo.svg"))//
			.put(CUMMINS, new ClientInfo("Cummins", true, true, "https://cummins.emoldino.com", "/images/common/logo/cummins-logo.svg"))//
			.put(DAIKIN, new ClientInfo("daikin", "Daikin", true, true, "https://daikin.emoldino.com", "/images/common/logo/daikin-logo.svg"))//
			.put(DELONGHI, new ClientInfo("delonghi", "Delonghi", true, true, "https://delonghi.emoldino.com", "/images/common/logo/delonghi-logo.svg"))//
			.put(DENSO_INDIA, new ClientInfo("densoin", "Denso India", true, true, "https://densoin.emoldino.com", "/images/common/logo/denso-logo.svg"))//
			.put(DENSO_ITALY, new ClientInfo("denso", "Denso Italy", true, true, "https://denso.emoldino.com", "/images/common/logo/denso-logo.svg"))//
			.put(DYSON, new ClientInfo("Dyson", true, true, "https://dyson.emoldino.com", "/images/common/logo/dyson-logo.svg"))//
			.put(EATON, new ClientInfo("Eaton", true, true, "https://eaton.emoldino.com", "/images/common/logo/eaton-logo.svg"))//
			.put(ELECTROLUX_BRAZIL, new ClientInfo("electroluxsa", "Electolux Brazil", true, true, "https://electroluxsa.emoldino.com", "/images/common/logo/electrolux-logo.svg"))//
			.put(FLEX, new ClientInfo("Flex", true, true, "https://flex.emoldino.com", "/images/common/logo/flex-logo.svg"))//
			.put(HOFMANN_PLASTICS, new ClientInfo("Hofman Plastics", true, true, "https://hofmannplastics.emoldino.com", "/images/common/logo/hofmann-logo.svg"))//
			.put(HONEYWELL, new ClientInfo("Honeywell", true, true, "https://honeywell.emoldino.com", "/images/common/logo/honeywell-logo.svg"))//
			.put(IROBOT, new ClientInfo("irobot", "iRobot", true, true, "https://irobot.emoldino.com", "/images/common/logo/irobot-logo.svg"))//
			.put(IVECO_GROUP, new ClientInfo("Iveco Group", true, true, "https://ivecogroup.emoldino.com", "/images/common/logo/iveco-logo.svg"))//
			.put(JABIL, new ClientInfo("jabil", "Jabil", true, true, "https://jabil.emoldino.com", "/images/common/logo/jabil-logo.svg"))//
			.put(JEILMETAL, new ClientInfo("Jeilmetal", true, true, "https://jeilmetal.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
			.put(JAGUAR, new ClientInfo("jaguar", "Jaguar", true, true, "https://jaguar.emoldino.com", "/images/common/logo/jaguar-logo.svg"))//
			.put(LOREAL, new ClientInfo("loreal", "Loreal", true, true, "https://loreal.emoldino.com", "/images/common/logo/loreal-logo.svg"))//
			.put(MABE, new ClientInfo("mabe", "Mabe", true, true, "https://mabe.emoldino.com", "/images/common/logo/mabe-logo.svg"))//
			.put(NESTLE, new ClientInfo("nestle", "Nestle", true, true, "https://nestle.emoldino.com", "/images/common/logo/nestle-logo.svg"))//
			.put(NISSAN, new ClientInfo("Nissan", true, true, "https://nissan.emoldino.com", "/images/common/logo/nissan-logo.svg"))//
			.put(PACCAR, new ClientInfo("Paccar", true, true, "https://paccar.emoldino.com", "/images/common/logo/paccar-logo.svg"))//
			.put(PERFETTI_VANMELLE, new ClientInfo("Perfetti Vanmelle", true, true, "https://perfettivanmelle.emoldino.com", "/images/common/logo/perfetti-logo.svg"))//
			.put(PG, new ClientInfo("pg", "P&G", true, true, "https://pg.emoldino.com", "/images/common/logo/p-&-g-logo.svg"))//
			.put(PHILIPS, new ClientInfo("Philips", true, true, "https://philips.emoldino.com", "/images/common/logo/versuni-logo.svg"))//
			.put(PLASTICOMNIUM, new ClientInfo("Plastic Omnium", true, true, "https://plasticomnium.emoldino.com", "/images/common/logo/plasticomnium-logo.svg"))//
			.put(RENAULT_TURKEY, new ClientInfo("Renault Turkey", true, true, "https://renault.emoldino.com", "/images/common/logo/renault-logo.svg"))//
			.put(SCHAEFFLER, new ClientInfo("Schaeffler", true, true, "https://schaeffler.emoldino.com", "/images/common/logo/schaeffler-logo.svg"))//
			.put(TESLA, new ClientInfo("Tesla", true, true, "https://tesla.emoldino.com", "/images/common/logo/tesla-logo.svg"))//
			.put(VESTEL, new ClientInfo("Vestel", true, true, "https://vestel.emoldino.com", "/images/common/logo/vestel-logo.svg"))//
			.put(VIBRACOUSTIC, new ClientInfo("Vibracoustic", true, true, "https://vibracoustic.emoldino.com", "/images/common/logo/vibracoustic-logo.svg"))//
			.put(ZEBRA, new ClientInfo("Zebra", true, true, "https://zebra.emoldino.com", "/images/common/logo/zebra-logo.svg"))//
			.put(ZF, new ClientInfo("ZF", true, true, "https://zf.emoldino.com", "/images/common/logo/zf-logo.svg"))//

//			DAYOUEP
//			DAYOUPLUS
//			DHK
//			ERAE

//			.put(ELECTROLUX_US, new ClientInfo("Electrolux US", true, false, "https://electroluxus.emoldino.com", "/images/common/logo/electrolux-logo.svg"))//
//			.put(FORD, new ClientInfo("Ford", "https://ford.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
//			.put(ICEE, new ClientInfo("ICEE", "https://icee.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
//			.put(MISUMI, new ClientInfo("Misumi", "https://misumi.emoldino.com", "/images/common/logo/emoldino-logo.svg"))//
//			.put(NICE, new ClientInfo("Nice", false, false, "https://nice.emoldino.com", "/images/common/logo/nice-logo.svg"))//
//			.put(RIL, new ClientInfo("RIL", true, true, "https://ril.emoldino.com", "/images/common/logo/ril-logo.svg"))//
//			.put(STANLEY_BD, new ClientInfo("Stanley BD", true, false, "https://sbd.emoldino.com", "/images/common/logo/stanley-logo.svg"))//
//			.put(VOLVO, new ClientInfo("Volvo", false, false, "https://volvo.emoldino.com", "/images/common/logo/volvo-logo.svg"))//

			.build();

	private static final Map<String, String> SERVER_NAME_BY_ALIAS = new LinkedHashMap<>();

	public static void load() {
		// For Class Loading

		CLIENT.forEach((serverName, client) -> {
			if (ObjectUtils.isEmpty(client.getAlias())) {
				return;
			}
			SERVER_NAME_BY_ALIAS.put(client.getAlias(), serverName);
		});
	}

	public static List<String> getCodes() {
		return new ArrayList<>(CLIENT.keySet());
	}

	private static String toServerName(String code) {
		if (code == null) {
			return null;
		}
		return SERVER_NAME_BY_ALIAS.containsKey(code) ? SERVER_NAME_BY_ALIAS.get(code) : code;
	}

	public static boolean exists(String code) {
		return CLIENT.containsKey(toServerName(code));
	}

	public static ClientInfo get(String code) {
		return CLIENT.get(toServerName(code));
	}

	private static final Map<String, ClientSensor> SENSOR;

	public static boolean existsSensor(String deviceId) {
		return SENSOR.containsKey(deviceId);
	}

	public static ClientSensor getSensor(String deviceId) {
		return SENSOR.get(deviceId);
	}

	public static String getForwardUrlBySensor(String deviceId) {
		if (!ClientUtils.existsSensor(deviceId)) {
			return null;
		}
		ClientSensor sensor = ClientUtils.getSensor(deviceId);
		if (sensor == null || sensor.getClientCode() == null || sensor.getClientCode().equals(ConfigUtils.getServerName())) {
			return null;
		}
		ClientInfo client = ClientUtils.get(sensor.getClientCode());
		return client == null ? null : client.getUrl();
	}

	@Data
	@NoArgsConstructor
	public static class ClientInfo {
		private String code;
		private String alias;
		private String name;
		private boolean emoldino;
		private boolean uploadEnabled;
		private boolean reportEnabled;
		private String url;
		private String logoPath;
		private Map<String, ClientSensor> sensors;

		public ClientInfo(String name, boolean uploadEnabled, boolean reportEnabled, String url, String logoPath) {
			this.name = name;
			this.uploadEnabled = uploadEnabled;
			this.reportEnabled = reportEnabled;
			this.url = url;
			this.logoPath = logoPath;
			this.sensors = new LinkedHashMap<>();
		}

		public ClientInfo(String alias, String name, boolean uploadEnabled, boolean reportEnabled, String url, String logoPath) {
			this.alias = alias;
			this.name = name;
			this.uploadEnabled = uploadEnabled;
			this.reportEnabled = reportEnabled;
			this.url = url;
			this.logoPath = logoPath;
			this.sensors = new LinkedHashMap<>();
		}

		public ClientInfo(String name, boolean emoldino, boolean uploadEnabled, boolean reportEnabled, String url, String logoPath) {
			this.name = name;
			this.emoldino = emoldino;
			this.uploadEnabled = uploadEnabled;
			this.reportEnabled = reportEnabled;
			this.url = url;
			this.logoPath = logoPath;
			this.sensors = new LinkedHashMap<>();
		}

		public ClientInfo add(ClientSensor sensor) {
			sensor.setClientCode(this.code);
			this.sensors.put(sensor.getDeviceId(), sensor);
			return this;
		}
	}

	@Data
	@AllArgsConstructor
	@Builder
	public static class ClientSensor {
		private String deviceId;
		private String modelNo;
		private String clientCode;
		private String distDate;

		public ClientSensor(String deviceId, String modelNo, Map<String, ClientSensor> sensors) {
			if (sensors.containsKey(deviceId)) {
				throw new LogicException("DUPLICATED_DEVICE_ID");
			}
			this.deviceId = deviceId;
			this.modelNo = modelNo;
			sensors.put(deviceId, this);
		}
	}

	static {
		CLIENT.forEach((code, client) -> client.setCode(code));
		Map<String, ClientSensor> sensors = new LinkedHashMap<>();

		CLIENT.get(DEMO)//
				.add(new ClientSensor("EMA2233M10186", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10187", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10195", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10201", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10205", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10206", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10211", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10216", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10227", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10232", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10235", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10236", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10237", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10241", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10245", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10247", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10248", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10251", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10252", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10254", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10258", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10261", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10262", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10266", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10267", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10272", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10275", "SS3", sensors))//
				.add(new ClientSensor("EMA2233M10284", "SS3", sensors))//
		;
		CLIENT.get(ABB_GERMANY)//
				.add(new ClientSensor("EMA2233M10401", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01003", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01005", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01006", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01007", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01009", "SS3", sensors))//
				.add(new ClientSensor("NCM2111I01013", "SS3", sensors))//
		;
		CLIENT.get(JAGUAR)//
				.add(new ClientSensor("NCM2207P01012", "SS3", sensors))//
				.add(new ClientSensor("NCM2207P01015", "SS3", sensors))//
				.add(new ClientSensor("NCM2207P01017", "SS3", sensors))//
				.add(new ClientSensor("NCM2244I01055", "SS3", sensors))//
		;
		CLIENT.get(LOREAL)//
				.add(new ClientSensor("EMA2252M10401", "SS3", sensors))//
				.add(new ClientSensor("EMA2252M10402", "SS3", sensors))//
				.add(new ClientSensor("EMA2252M10403", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01112", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01113", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01116", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01119", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01120", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01050", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01063", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01064", "SS3", sensors))//
				.add(new ClientSensor("NCM2115I01066", "SS3", sensors))//
				.add(new ClientSensor("NCM2117I01018", "SS3", sensors))//
				.add(new ClientSensor("NCM2117I01019", "SS3", sensors))//
				.add(new ClientSensor("NCM2117I01021", "SS3", sensors))//
				.add(new ClientSensor("NCM2117I01023", "SS3", sensors))//
				.add(new ClientSensor("NCM2141I01296", "SS3", sensors))//
				.add(new ClientSensor("NCM2207P01002", "SS3", sensors))//
				.add(new ClientSensor("NCM2207P01001", "SS3", sensors))//
		;
		CLIENT.get(PHILIPS)//
				.add(new ClientSensor("NCM2234I01291", "SS3", sensors))//
				.add(new ClientSensor("NCM2234I01294", "SS3", sensors))//
		;
		CLIENT.get(PG)//
				.add(new ClientSensor("EMA2233M10287", "SS3", sensors))//
		;
		CLIENT.get(SCHAEFFLER)//
				.add(new ClientSensor("EMA2252M10143", "SS3", sensors))//
				.add(new ClientSensor("NCM2024I01001", "SS3", sensors))//
				.add(new ClientSensor("NCM2024I01004", "SS3", sensors))//
				.add(new ClientSensor("NCM2214I01218", "SS3", sensors))//
				.add(new ClientSensor("NCM2234I01296", "SS3", sensors))//
		;

		SENSOR = sensors;
	}
}
