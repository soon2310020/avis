package com.emoldino.framework.terminology.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.LogicUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TeminologyUtils {
	private static final List<Terminology> LIST = Arrays.asList(//

			new Terminology("accumulated", "accum", "acc"), //
			new Terminology("accumulative", "accum", "acc"), //
			new Terminology("alert", "alert", "alr"), //
			new Terminology("analysis", "analysis", "ana"), //
			new Terminology("approval", "appr", "apr"), //
			new Terminology("approve", "appr", "apr"), //
			new Terminology("audit", "audit", "adt"), //

			new Terminology("bell", "bell", "bel"), //
			new Terminology("budget", "budget", "bud"), //

			new Terminology("capacity", "capa", "cap"), //
			new Terminology("central", "centr", "cen"), //
			new Terminology("change", "chg", "chg"), //
			new Terminology("chart", "chart", "cht"), //
			new Terminology("company", "company", "com"), //
			new Terminology("completion", "completion", "cpt"), //
			new Terminology("compliance", "compl", "cpl"), //
			new Terminology("component", "comp", "cmp"), //
			new Terminology("config", "conf", "stp"), //
			new Terminology("confirm", "confirm", "cfm"), //
			new Terminology("confirmation", "confirm", "cfm"), //
			new Terminology("confirmed", "confirm", "cfm"), //
			new Terminology("creation", "creation", "crt"), //
			new Terminology("currency", "currency", "cur"), //
			new Terminology("cycle", "cycle", "cyc"), //

			new Terminology("dashboard", "dashboard", "dsh"), //
			new Terminology("demand", "demand", "dem"), //
			new Terminology("detachment", "detachment", "det"), //
			new Terminology("disconnected", "discon", "dco"), //
			new Terminology("disconnection", "discon", "dco"), //
			new Terminology("display", "display", "dsp"), //
			new Terminology("downtime", "downtime", "dtm"), //

			new Terminology("endOfLife", "eol", "eol"), //
			new Terminology("estimated", "estim", "est"), //

			new Terminology("hierarchy", "hier", "hie"), //
			new Terminology("history", "hist", "his"), //

			new Terminology("inactive", "inactive", "ina"), //

			new Terminology("language", "lang", "lan"), //

			new Terminology("maximum", "max", "max"), //

			new Terminology("notification", "noti", "not"), //

			new Terminology("operation", "oper", "opr"), //
			new Terminology("operational", "oper", "opr"), //
			new Terminology("order", "order", "ord"), //
			new Terminology("overall", "overall", "ovr"), //

			new Terminology("part", "part", "par"), //
			new Terminology("pattern", "pattern", "pat"), //
			new Terminology("planning", "planning", "pln"), //
			new Terminology("plant", "plant", "plt"), //
			new Terminology("predicted", "pred", "pre"), //
			new Terminology("process", "proc", "pro"), //
			new Terminology("produced", "prod", "prd"), //
			new Terminology("production", "prod", "prd"), //
			new Terminology("productionPatternAnalysis", "ppa", "ppa"), //

			new Terminology("quality", "qual", "qua"), //
			new Terminology("quantity", "qty", "qty"), //

			new Terminology("reference", "ref", "ref"), //
			new Terminology("refurbishment", "refurb", "rfb"), //
			new Terminology("relocation", "relocation", "rlo"), //
			new Terminology("report", "report", "rpt"), //
			new Terminology("reset", "reset", "rst"), //
			new Terminology("risk", "risk", "rsk"), //
			new Terminology("role", "role", "rol"), //

			new Terminology("sensor", "sensor", "ssr"), //
			new Terminology("strategy", "strategy", "str"), //
			new Terminology("system", "sys", "sys"), //

			new Terminology("terminal", "terminal", "tmn"), //
			new Terminology("time", "time", "tim"), //
			new Terminology("tool", "tool", "tol"), //
			new Terminology("totalCostOfOwnership", "tco", "tco"), //

			new Terminology("user", "user", "usr"), //
			new Terminology("utilization", "util", "utl"), //

			new Terminology("widget", "widget", "wgt"), //
			new Terminology("work", "work", "wrk"), //

			new Terminology("zzzzz", "zzz")//

	);

	private static final Map<String, Terminology> MAP;
	private static final MultiValueMap<String, Terminology> VALUE_MAP;
	private static final MultiValueMap<String, Terminology> ABBR_MAP;
	private static final MultiValueMap<String, Terminology> ABBR3CHAR_MAP;
	static {
		Map<String, Terminology> map = new LinkedHashMap<>();
		MultiValueMap<String, Terminology> valueMap = new LinkedMultiValueMap<>();
		MultiValueMap<String, Terminology> abbrMap = new LinkedMultiValueMap<>();
		MultiValueMap<String, Terminology> abbr3CharMap = new LinkedMultiValueMap<>();
		LIST.forEach(item -> {
			LogicUtils.assertNotEmpty(item.getKey(), "key");

			if (map.containsKey(item.getKey())) {
				throw new LogicException("DUPLICATED_TERMINOLOGY", item.getKey() + " terminology has been duplicated!!");
			}

			map.put(item.getKey(), item);
			if (!ObjectUtils.isEmpty(item.getValue())) {
				valueMap.add(item.getValue(), item);
			}
			if (!ObjectUtils.isEmpty(item.getAbbr())) {
				abbrMap.add(item.getAbbr(), item);
			}
			if (!ObjectUtils.isEmpty(item.getAbbr3Char())) {
				abbr3CharMap.add(item.getAbbr3Char(), item);
			}
		});
		MAP = map;
		VALUE_MAP = valueMap;
		ABBR_MAP = abbrMap;
		ABBR3CHAR_MAP = abbr3CharMap;
	}

	public static Terminology getByKey(String key) {
		return MAP.get(key);
	}

	public static List<Terminology> getByValue(String value) {
		return VALUE_MAP.get(value);
	}

	public static List<Terminology> getByAbbr(String abbr) {
		return ABBR_MAP.get(abbr);
	}

	public static List<Terminology> getByAbbr3Char(String abbr3Char) {
		return ABBR3CHAR_MAP.get(abbr3Char);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Terminology {
		private String key;
		private String value;
		private String abbr;
		private String abbr3Char;

		public Terminology(String key, String abbr3Char) {
			this(key, key, abbr3Char, abbr3Char);
		}

		public Terminology(String key, String abbr, String abbr3Char) {
			this(key, key, abbr, abbr3Char);
		}
	}
}
