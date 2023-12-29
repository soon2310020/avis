package saleson;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import saleson.common.util.StringUtils;

@Slf4j
public class AppTest2 {

	@Test
	public void temp() {
		Map<String, Object> map = new HashMap<>();
		map.put("counterId", "23423");
		map.put("counterId2", "23423");
		map.put("xxx", "23423");

		log.debug("Transfer Map Data : {}", map);

		map.remove("counterId2");
		log.debug("Transfer Map Data 2 : {}", map);

		System.out.println(map.get("counterId"));
		System.out.println(map.get("counterId2"));

		String temp = "201/205/203/204/204/20190820202421/210";
		String[] values = StringUtils.delimitedListToStringArray(temp, "/");

		String date = values[5];
		Integer max = Arrays.stream(values)//
				.filter(s -> !date.equals(s))//
				.map(s -> Integer.parseInt(s))//
				.max(Comparator.comparing(Integer::valueOf)).get();
		Integer min = Arrays.stream(values)//
				.filter(s -> !date.equals(s))//
				.map(s -> Integer.parseInt(s))//
				.min(Comparator.comparing(Integer::valueOf)).get();

		System.out.println(min + " :: " + max);
	}
}
