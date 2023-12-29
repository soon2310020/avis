package saleson.common.scheduling;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchJobTest {

	@Autowired
	BatchJob batchJob;

	@Test
	public void updateMoldOperatingStatusTest() {
		batchJob.updateOperatingStatus();
	}

	@Test
	public void streamTest() {
		List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> data2 = Arrays.asList(2, 4);

		data.stream().filter(d -> {
			long matchedCount = data2.stream().filter(d2 -> d == d2).count();
			return matchedCount <= 0 ? true : false;
		}).forEach(d -> System.out.println(d));

	}
}