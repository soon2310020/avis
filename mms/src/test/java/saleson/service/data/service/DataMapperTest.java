package saleson.service.data.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;

class DataMapperTest {

	private DataMapper dataMapper;
	private Data data;

	@BeforeEach
	void setUp() {
		dataMapper = new DataMapper();
		data = new Data();
		data.setTerminalId("T0001");
	}

	@Test
	void toDataAccelerationTest() {
		String rawData = "ADATA/SC_0000001/00007/" + "20210901004159/0.0,0.098,0.6,-0.003,1.9,0.037,2.4,0.013,6.4,0.020,31.7,0.003,31.8,0.011,32.3,0.045/"
				+ "20210901004558/0.0,0.081,0.6,-0.003,1.9,0.037,2.4,0.014,6.4,0.020,31.7,0.004,31.9,0.011,32.3,0.049/"
				+ "20210901005009/0.1,-0.002,0.5,-0.004,1.8,0.036,2.3,0.012,6.3,0.019,31.6,0.007,31.7,0.012,32.2,0.049/"
				+ "20210901005337/0.0,0.010,0.5,-0.004,1.8,0.034,2.3,0.014,6.3,0.018,31.6,0.008,31.8,0.012,32.2,0.050/"
				+ "20210901005705/0.0,0.012,0.6,-0.004,1.9,0.036,2.3,0.012,6.3,0.016,31.6,0.009,31.8,0.014,32.3,0.047/"
				+ "20210901010033/0.0,0.070,0.6,-0.005,1.9,0.035,2.3,0.014,6.3,0.018,31.6,0.010,31.8,0.015,32.3,0.047/"
				+ "20210901010400/0.0,0.105,0.6,-0.005,1.9,0.035,2.4,0.013,6.4,0.017,31.7,0.010,31.9,0.009,32.3,0.051";

		data.setRawData(rawData);

		List<DataAcceleration> dataAccelerations = dataMapper.toDataAccelerationList(data);

		assertThat(dataAccelerations.size()).isEqualTo(7);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().size()).isEqualTo(8);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().get(7).getValue()).isEqualTo("0.051");
	}

	@Test
	void toDataAccelerationTest2() {
		String rawData = "ADATA/SC_0000001/00007/20210901004159/0.0,0.098,0.6,-0.003,1.9,0.037,2.4,0.013,6.4,0.020,31.7,0.003,31.8,0.011,32.3,0.045/20210901004558";

		data.setRawData(rawData);

		List<DataAcceleration> dataAccelerations = dataMapper.toDataAccelerationList(data);

		assertThat(dataAccelerations.size()).isEqualTo(1);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().size()).isEqualTo(8);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().get(7).getValue()).isEqualTo("0.045");

	}

	@Test
	void toDataAccelerationTest3() {
		String rawData = "ADATA/SC_0000001/00007/20210901004159/0.0,0.098,0.6,-0.003,1.9,0.037,2.4,0.013,6.4,0.020,31.7,0.003,31.8,0.011,32.3/20210901004558";

		data.setRawData(rawData);

		List<DataAcceleration> dataAccelerations = dataMapper.toDataAccelerationList(data);

		dataAccelerations.forEach(System.out::println);

		assertThat(dataAccelerations.size()).isEqualTo(1);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().size()).isEqualTo(7);
		assertThat(dataAccelerations.get(dataAccelerations.size() - 1).getAccelerations().get(6).getValue()).isEqualTo("0.011");
	}

	@Test
	void index() {
		String rawData = "" + "CDATA/" + "TMS1919KRB002/" + "CMS1919I04001/" + "20210901003801/" + "20210901010738/" + "0000040/" + "9/" + "4/" + "105310551067106910741085/"
				+ "0084/" + "0414041604240430044004420452046404680502051805260538054405820618/" + "jfhgkmfcendoipdab4ab5ab5ab4al";

//		String text = "" + "CDATA/" + "게이트웨이ID/" + "카운터ID/" + "Shot시작시간/" + "shot종료시간/" + "최종shot수/" + "배터리상태/" + "상태정보(통신여부)/" + "온도/" + "최빈mold open time/" + "인덱싱테이블/" + "사이클타임인덱싱결과";

		DataCounter data = dataMapper.toDataCounter(rawData);
		System.out.println(data);

		data.getCycleTimes().forEach(System.out::println);
	}

	@Test
	void substringTest() {
		String cycleTimeIndexing = "jfhgkmfcendoipdad4ad5ad5ad4al";

		for (String s : cycleTimeIndexing.split("")) {
			System.out.println(s);
		}

		for (int i = 0; i < cycleTimeIndexing.length(); i++) {
			String indexKey = cycleTimeIndexing.substring(i, i + 1);

			int nextIndex = i + 2 > cycleTimeIndexing.length() ? i + 1 : i + 2;
			String repeatKey = cycleTimeIndexing.substring(i + 1, nextIndex);

			int repeat = 1;
			if (!repeatKey.isEmpty() && repeatKey.chars().allMatch(Character::isDigit)) {
				repeat = Integer.parseInt(repeatKey);
			}

			System.out.println(indexKey + " : " + repeatKey + " : " + repeat);
		}
	}

	@Test
	void map() {
		List<DataCounter> dataCounters = new ArrayList<>();
		List<DataAcceleration> dataAccelerations = new ArrayList<>();

		Data d = new Data();
		d.setTerminalId("T001");
		d.setRawData("CDATA/SC_0000004/20211125142225/20211125152202/00050/9/4/040804100411041304150416/0138/07140716/b3ab2ab3ab2ab3ab2ab3ab2ab3ab3ab2ab3ab2ab2a" + "/"
				+ "ADATA/SC_0000004/00010/20211125142823/0.7,0.089,1.1,0.003,2.7,0.025,4.0,0.008,6.0,0.026,56.4,0.000,56.6,0.011,56.9,0.030/20211125143421/0.6,0.088,1.1,0.004,2.6,0.020,3.9,0.010,5.9,0.026,56.3,0.000,56.5,0.008,56.8,0.031/20211125144018/0.7,0.074,1.2,0.002,2.8,0.023,4.0,0.009,6.0,0.030,56.4,0.000,56.6,0.030,56.8,0.106/20211125144616/0.6,0.064,1.1,0.004,2.7,0.021,4.0,0.008,5.9,0.031,56.1,0.013,56.5,0.010,56.9,0.028/20211125145214/0.7,0.081,1.2,0.005,2.8,0.018,4.1,0.009,6.0,0.031,56.2,0.008,56.6,0.010,56.8,0.032/20211125145811/0.7,0.072,1.1,0.004,2.7,0.025,4.0,0.010,6.0,0.029,56.2,0.016,56.5,0.025,56.7,0.082/20211125150409/0.6,0.074,1.1,0.004,2.6,0.027,3.9,0.011,5.9,0.029,56.2,0.000,56.4,0.009,56.8,0.034/20211125151007/0.7,0.074,1.2,0.004,2.7,0.023,4.0,0.010,6.0,0.031,56.6,0.013,56.9,0.035/20211125151605/0.6,0.082,2.6,0.025,3.9,0.010,5.9,0.028,56.4,0.011,56.8,0.040/20211125152202/0.7,0.068,1.2,0.004,2.8,0.025,4.0,0.010,6.0,0.029,56.6,0.011,56.9,0.037"
				+ "/"
				+ "TEST/0.0/57.8/71.6/129.2/143.2/200.8/214.8/272.4/286.2/344.0/357.8/415.4/429.4/487.0/500.8/558.6/572.4/630.1/644.0/701.6/715.6/773.2/787.0/844.8/858.6/916.2/930.2/987.8/1001.6/1059.4/1073.2/1130.8/1144.8/1202.4/1216.4/1274.1/1287.8/1345.6/1359.4/1417.0/1431.0/1488.6/1502.4/1560.2/1574.0/1631.6/1645.6/1703.2/1717.2/1774.8/1788.6/1846.4/1860.2/1917.9/1931.8/1989.4/2003.2/2061.0/2074.8/2132.4/2146.4/2204.0/2218.0/2275.6/2289.4/2347.2/2361.0/2418.6/2432.6/2490.2/2504.2/2561.9/2575.6/2633.2/2647.2/2704.8/2718.8/2776.4/2790.2/2848.0/2861.8/2919.4/2933.4/2991.0/3005.0/3062.6/3076.4/3134.0/3148.0/3205.7/3219.6/3277.2/3291.0/3348.8/3362.6/3420.2/3434.2/3491.8/3505.6/3563.4");

		String rawdata = d.getRawData();

		// 데이터가 붙어서 오는 경우 (CDATA/..../ADATA/....)가 있어 데이터 종류별로 파싱하여 map에 저장.
		Map<String, String> map = new LinkedHashMap<>();
		map.put(DataMapper.TDATA, "");
		map.put(DataMapper.ADATA, "");
		map.put(DataMapper.CDATA, "");

		for (Map.Entry<String, String> e : map.entrySet()) {
			if (rawdata.indexOf(e.getKey()) > -1) {
				int beginIndex = rawdata.indexOf(e.getKey());
				String parsedData = rawdata.substring(beginIndex);

				rawdata = rawdata.substring(0, beginIndex == 0 ? 0 : beginIndex - 1);
				map.put(e.getKey(), parsedData);
			}
		}

		if (!map.get(DataMapper.CDATA).isEmpty()) {
			DataCounter dc = dataMapper.toDataCounter(map.get(DataMapper.CDATA));
			if (dc != null) {
				dc.setTerminalId(d.getTerminalId());
				dc.setRawdataCreatedAt(d.getUpdatedDate());
				dc.setDataId(d.getId());

				dataCounters.add(dc);
			}
		}

		if (!map.get(DataMapper.ADATA).isEmpty()) {
			List<DataAcceleration> accelerationList = dataMapper.toDataAccelerationList(map.get(DataMapper.ADATA));
			accelerationList.forEach(acceleration -> {
				acceleration.setDataId(d.getId());
				acceleration.setTerminalId(d.getTerminalId());
				acceleration.setRawdataCreatedAt(d.getUpdatedDate());

				dataAccelerations.add(acceleration);
			});
		}

		map.forEach((key, value) -> System.out.println(key + ": " + value));
	}
}