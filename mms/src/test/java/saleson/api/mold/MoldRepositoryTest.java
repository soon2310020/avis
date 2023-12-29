package saleson.api.mold;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import saleson.api.mold.payload.MoldPayload;
import saleson.model.data.MapChartData;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MoldRepositoryTest {
	@Autowired
	MoldRepository moldRepository;


	@Test
	public void mapChartTest() {
		MoldPayload payload = new MoldPayload();

		List<MapChartData> mapDataList = moldRepository.getMapData(payload);


		mapDataList.stream().forEach(d -> System.out.println(d));

	}

}