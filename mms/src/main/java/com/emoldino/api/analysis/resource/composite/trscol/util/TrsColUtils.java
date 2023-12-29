package com.emoldino.api.analysis.resource.composite.trscol.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrsColUtils {

	public static void parseCtt(Map<String, Object> map) {
		String ctt = (String) map.get("ctt");
		double ct = Double.parseDouble((String) map.get("ct"));

		String[] ctts = StringUtils.delimitedListToStringArray(ctt, "/");

		double lowerCt = 0;
		double lowerShot = 0;
		double upperCt = 0;
		double upperShot = 0;
		for (int i = 0; i < ctts.length; i++) {
			if (i % 2 == 1) {
				continue;
			}

			double cycleTime = ctts[i].isEmpty() || "*".equals(ctts[i]) ? 0 : Double.parseDouble(ctts[i]);
			double shot = ctts[i + 1].isEmpty() || "*".equals(ctts[i + 1]) ? 0 : Double.parseDouble(ctts[i + 1]);
			// double computedCt = (cycleTime - ct) * shot;  // 증가분으로 표기
			double computedCt = (cycleTime) * shot; // CT + 증가분에 대한 결과(100ms)로 표기

			if (cycleTime - ct < 0) {
				lowerCt += computedCt;
				lowerShot += shot;
			} else if (cycleTime - ct > 0) {
				upperCt += computedCt;
				upperShot += shot;
			}
		}

		//double ulct = 0, llct = 0;	// 증가분으로 표기
		double ulct = ct, llct = ct;
		if (upperShot > 0)
			ulct = upperCt / upperShot;
		if (lowerShot > 0)
			llct = lowerCt / lowerShot;

		map.put("ulct", Math.round(ulct));
		map.put("llct", Math.round(llct));
	}

	/**
	 * R2 버전에서 일부 데이터가 / 구분자로 묶여서 넘어옴. (분기 로직 처리)
	 * @param map
	 * @param k
	 * @param v
	 */
	public static void bind(Map<String, Object> map, String k, String v) {

		if (!hasDelimiter(v)) {
			// 멀티 밸류값 중 첫번째만.
			map.put(k, v);
			return;
		}

		// * 값은 데이터가 없거나 0인 경우임
		v = v.replaceAll("\\*", "");
		String[] values = StringUtils.delimitedListToStringArray(v, "/");

		if ("id".equals(k) && values.length == 2) {
			// [TDATA] id = ti / tv    ;    [CDATA] id = ti / ci
			map.put("ti", values[0]);
			map.put("idSecondValue", values[1]);

		} else if ("net".equals(k) && values.length == 6) { // [TDATA] net = nt / dh / ip / gw / dn / dn2
			map.put("nt", values[0]);
			map.put("dh", values[1]);
			map.put("ip", values[2]);
			map.put("gw", values[3]);
			map.put("dn", values[4]);
			map.put("dn2", values[5]);

		} else if ("time".equals(k) && values.length == 2) { // [CDATA] time = lst / rt
			map.put("lst", values[0]);
			map.put("rt", values[1]);

		} else if ("ci".equals(k) && values.length == 3) { // [CDATA] ci = cf / ct / bs
			map.put("cf", values[0]);
			map.put("ct", values[1]);
			map.put("bs", values[2]);

		} else if ("ctt".equals(k)) { // [CDATA] ctt 는 그대로 저장
			map.put("ctt", v);

		} else if ("rmi".equals(k) && values.length == 5) { // [CDATA] rmi = rcn / rtr / rtf / rtl / rat
			map.put("rcn", values[0]);
			map.put("rtr", values[1]);
			map.put("rtf", values[2]);
			map.put("rtl", values[3]);
			map.put("rat", values[4]);

		} else if ("temp".equals(k) && values.length == 7) { // [CDATA] temp = 10분단위 온도1 / 온도2 / 온도3 / 온도4 / 온도5 / 온도1 시간 / 현재온도(tnw) / rtr / rtf / rtl / rat
			map.put("temp", v);

			String tff = values[5];
			String current = values[6];

			Long tempCount = Arrays.stream(values).filter(s -> !tff.equals(s) && !"".equals(s)).count();

			Integer tlo = null;
			Integer thi = null;
			Integer tav = null;

			if (tempCount > 0) {
				tlo = Arrays.stream(values).filter(s -> !tff.equals(s) && !"".equals(s)).map(s -> Integer.parseInt(s)).min(Comparator.comparing(Integer::valueOf)).orElse(0);

				thi = Arrays.stream(values).filter(s -> !tff.equals(s) && !"".equals(s)).map(s -> Integer.parseInt(s)).max(Comparator.comparing(Integer::valueOf)).orElse(0);

				// 평균 온도
				OptionalDouble optionalDouble = IntStream.range(0, values.length).filter(i -> !(i == 5 || values[i].isEmpty() || "*".equals(values[i])))
						.map(i -> Integer.parseInt(values[i])).average();

				if (optionalDouble.isPresent()) {
					tav = (int) Math.round(optionalDouble.getAsDouble());
				}
			}

			map.put("tlo", tlo);
			map.put("thi", thi);
			map.put("tav", tav);
			map.put("tff", tff);
			map.put("tnw", "".equals(current) ? null : current);

		} else if ("ei".equals(k) && values.length == 2) { // [CDATA] ei = ac / pw(내가 추가함)
			map.put("ac", values[0]);
			map.put("epw", values[1]);

		} else if ("acc".equals(k)) { // [CDATA] acc는 그대로 저장
			map.put("acc", v);
		}
	}

	private static boolean hasDelimiter(String v) {
		return v != null && v.indexOf("/") > -1;
	}

}
