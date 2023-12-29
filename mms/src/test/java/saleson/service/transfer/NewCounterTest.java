package saleson.service.transfer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class NewCounterTest {

	@Test
	void cttCompute() {

		int ct = 497;
		String ctt = "497/25/503/4/512/2/516/2/520/2/521/4/531/3/534/1/536/2/541/9";
		parseCtt(ct, ctt);


		log.error("=============================");

		parseCtt2(ct, ctt);

	}

	@Test
	void cttTest2() {
		List<Integer> cts = Arrays.asList(
			507,626,497,547,621,601,527,547,496,513,546,750,749,749,749,749,749
		);

		List<String> ctts = Arrays.asList(
			"454/6/468/4/470/1/471/2/477/2/482/3/506/7/507/12/573/7/574/5",
			"521/1/523/1/524/1/531/1/537/13/626/14/628/1/629/2/642/6/956/2",
			"497/25/503/4/512/2/516/2/520/2/521/4/531/3/534/1/536/2/541/9",
			"419/1/423/2/428/1/430/2/456/1/460/1/466/1/516/3/547/12/562/4",
			"621/29/626/1/627/2/640/5/659/1/661/1/665/1/667/2/1231/1/2567/1",
			"477/1/487/1/524/1/527/0/543/1/553/1/579/3/596/1/601/22/1522/2",
			"527/2/974/0/2314/1//////////////",
			"547/13/550/2/551/3/554/2/555/1/557/1/558/2/571/9/579/5/799/5",
			"496/34/498/1/499/1/502/1/543/3/544/1/545/4/546/2/569/4/1088/3",
			"513/31/541/3/544/6/545/2/547/1/564/5/576/2/577/1/600/2/629/2",
			"435/2/498/1/514/15/522/1/525/3/526/2/529/4/546/17/641/12/1217/1",
			"749/7/750/9/950/1//////////////",
			"350/1/371/1/419/0/749/5/750/5/756/1////////",
			"350/1/371/1/419/0/749/5/750/5/756/1////////",
			"350/1/371/1/419/0/749/5/750/5/756/1////////",
			"350/1/371/1/419/0/749/5/750/5/756/1////////",
			"350/1/371/1/419/0/749/5/750/5/756/1////////"
		);

		for (int i = 0; i < ctts.size(); i++) {
			System.out.println();
			System.out.println();
			System.out.println();
			int ct = cts.get(i);
			String ctt = ctts.get(i);
			parseCtt(ct, ctt);
			parseCtt2(ct, ctt);
		}

	}

	@Test
	void cttTest() {
		// -48.605, 83.5666
		int ct = 640;
		String ctt = "73/3/74/8/214/3/222/4/369/2/640/19/878/1/1103/1/2447/1/3000/0";
		parseCtt(ct, ctt);

		// -0.1, x
		ct = 660;
		ctt = "659/20/660/34/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		// -0.1052631579, x
		ct = 660;
		ctt = "658/1/659/18/660/34/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 1-4");
		ct = 660;
		ctt = "659/20/660/33/661/1/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data 1-5");
		ct = 660;
		ctt = "659/22/660/29/661/2/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 1-6");
		ct = 660;
		ctt = "659/22/660/30/661/2/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 1-7");
		ct = 660;
		ctt = "659/23/660/28/661/3/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data 1-8");
		ct = 660;
		ctt = "659/20/660/23/661/3/714/1/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-1");
		ct = 0;
		ctt = "*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-2");
		ct = 410;
		ctt = "135/0/360/1/409/20/410/50/411/3/415/1/501/1/516/1/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data 2-3");
		ct = 410;
		ctt = "409/28/410/51/411/8/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-4");
		ct = 410;
		ctt = "409/21/410/65/411/1/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-5");
		ct = 410;
		ctt = "409/20/410/67/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-6");
		ct = 410;
		ctt = "409/21/410/64/411/2/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-7");
		ct = 410;
		ctt = "409/24/410/57/411/5/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-8");
		ct = 410;
		ctt = "409/24/410/57/411/5/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-9");
		ct = 410;
		ctt = "409/26/410/54/411/7/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 2-10");
		ct = 410;
		ctt = "409/27/410/52/411/8/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data 3-1");
		ct = 460;
		ctt = "26/0/452/1/457/1/459/14/460/40/573/1/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data 3-2");
		ct = 460;
		ctt = "459/27/460/43/461/7/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 3-3");
		ct = 460;
		ctt = "459/24/460/49/461/4/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 3-4");
		ct = 460;
		ctt = "459/25/460/48/461/5/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);

		log.debug("law data 3-5");
		ct = 460;
		ctt = "459/25/460/47/461/5/*/*/*/*/*/*/*/*/*/*/*/*/*/*";
		parseCtt(ct, ctt);


		log.debug("law data xxxx");
		ct = 460;
		ctt = null;
		parseCtt(ct, ctt);
	}

	private void parseCtt(int ct, String ctt) {
		String[] ctts = StringUtils.delimitedListToStringArray(ctt, "/");

		double lowerCt = 0;
		double lowerShot = 0;
		double upperCt = 0;
		double upperShot = 0;
		for (int i = 0; i < ctts.length; i++) {
			if (i % 2 == 1) continue;

			double cycleTime = ctts[i].isEmpty() || "*".equals(ctts[i]) ? 0 : Double.parseDouble(ctts[i]);
			double shot = ctts[i + 1].isEmpty() || "*".equals(ctts[i + 1]) ? 0 : Double.parseDouble(ctts[i + 1]);
			// double computedCt = (cycleTime - ct) * shot;  // 증가분으로 표기
			double computedCt = (cycleTime) * shot;  // CT + 증가분에 대한 결과(100ms)로 표기

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
		if (upperShot > 0) ulct = upperCt / upperShot;
		if (lowerShot > 0) llct = lowerCt / lowerShot;


		log.debug("==================================");
		log.debug("{} : {}", ct, ctt);
		log.debug("{} ~ {}", Math.round(llct), Math.round(ulct));
		//log.debug("{} ~ {}", ct + Math.round(llct), ct + Math.round(ulct));
		log.debug("");
	}
	private void parseCtt2(int ct, String ctt) {
		String[] ctts = StringUtils.delimitedListToStringArray(ctt, "/");

		double lowerCt = 0;
		double lowerShot = 0;
		double upperCt = 0;
		double upperShot = 0;
		for (int i = 0; i < ctts.length; i++) {
			if (i % 2 == 1) continue;

			double cycleTime = ctts[i].isEmpty() || "*".equals(ctts[i]) ? 0 : Double.parseDouble(ctts[i]);
			double shot = ctts[i + 1].isEmpty() || "*".equals(ctts[i + 1]) ? 0 : Double.parseDouble(ctts[i + 1]);
			double computedCt = cycleTime * shot;

			if (cycleTime - ct < 0) {
				lowerCt += computedCt;
				lowerShot += shot;
			} else if (cycleTime - ct > 0) {
				upperCt += computedCt;
				upperShot += shot;
			}
		}

		double ulct = ct, llct = ct;
		if (upperShot > 0) ulct = upperCt / upperShot;
		if (lowerShot > 0) llct = lowerCt / lowerShot;
		log.debug("==================================");
		log.debug("{} : {}", ct, ctt);
		log.debug("{} ~ {}", Math.round(llct), Math.round(ulct));
		log.debug("");
	}

	private void parseCtt(Map<String, Object> map) {
		if (map != null) {
			String ctt = (String) map.get("ctt");
			int ct = (int) map.get("ct");

			String[] ctts = StringUtils.delimitedListToStringArray(ctt, "/");

			double lowerCt = 0;
			double lowerShot = 0;
			double upperCt = 0;
			double upperShot = 0;
			for (int i = 0; i < ctts.length; i++) {
				if (i % 2 == 1) continue;

				double cycleTime = ctts[i].isEmpty() || "*".equals(ctts[i]) ? 0 : Double.parseDouble(ctts[i]);
				double shot = ctts[i + 1].isEmpty() || "*".equals(ctts[i + 1]) ? 0 : Double.parseDouble(ctts[i + 1]);
				double computedCt = (cycleTime - ct) * shot;

				if (cycleTime - ct < 0) {
					lowerCt += computedCt;
					lowerShot += shot;
				} else if (cycleTime - ct > 0) {
					upperCt += computedCt;
					upperShot += shot;
				}
			}

			Double ulct = null, llct = null;
			if (upperShot > 0) ulct = upperCt / upperShot;
			if (lowerShot > 0) llct = lowerCt / lowerShot;

			log.debug("==================================");
			log.debug("{} : {}", ct, ctt);
			log.debug("{} - {}", llct, ulct);
			log.debug("");
		}
	}



	@Test
	void tempTest() {
		String temp = "320/320/318/312/297/20200326183202/289";

		temp = "*/10/*/*/*/*/100";
		String[] values = StringUtils.delimitedListToStringArray(temp,"/");
		String date = values[5];


		Double avg = null;
		OptionalDouble optionalDouble = IntStream.range(0, values.length)
				.filter(i -> !(i == 5 || values[i].isEmpty() || "*".equals(values[i])))
				.map(i -> Integer.parseInt(values[i]))
				.average();

		if (optionalDouble.isPresent()) {
			avg = optionalDouble.getAsDouble();
			log.debug("AVG : {}", (int) Math.round(avg));
		} else {
			log.debug("AVG : {}", avg);
		}

	}

	@Test
	public void streamTest() {

		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);


		List<Integer> newList = list.stream()
				.filter(i -> i % 2 == 0)
				.collect(Collectors.toList());

		List<Integer> newList2 = list.stream()
				.filter(i -> i % 2 != 0)
				.collect(Collectors.toList());

		assertThat(list.size()).isEqualTo(5);
		assertThat(newList.size()).isEqualTo(2);
		assertThat(newList2.size()).isEqualTo(3);
	}

}
