package com.emoldino.framework.util;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.emoldino.framework.util.enumeration.AppType;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import saleson.common.enumeration.EquipmentStatus;

@Slf4j
public class ValueUtilsTest {

	@Test
	public void testToVersion() {
		// null, 0, 0-RELEASE, 0.0, 0.0-RELEASE, 0.0.0, 0.0.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo(null), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0"), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0-RELEASE"), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0"), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0-RELEASE"), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.0"), 99L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.0-RELEASE"), 99L);
		Assertions.assertEquals(ValueUtils.toVersion(99L), "0.0.0-RELEASE");

		// 0.0.1, 0.0.1-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.1"), 199L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.1-RELEASE"), 199L);
		Assertions.assertEquals(ValueUtils.toVersion(199L), "0.0.1-RELEASE");
		// 0.0.2, 0.0.2-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.2"), 299L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.2-RELEASE"), 299L);
		Assertions.assertEquals(ValueUtils.toVersion(299L), "0.0.2-RELEASE");
		// 0.0.10, 0.0.10-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.10"), 1099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.0.10-RELEASE"), 1099L);
		Assertions.assertEquals(ValueUtils.toVersion(1099L), "0.0.10-RELEASE");

		// 0.1, 0.1.0, 0.1-RELEASE, 0.1.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.1"), 10099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.1.0"), 10099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.1-RELEASE"), 10099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.1.0-RELEASE"), 10099L);
		Assertions.assertEquals(ValueUtils.toVersion(10099L), "0.1.0-RELEASE");
		// 0.2, 0.2.0, 0.2-RELEASE, 0.2.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.2"), 20099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.2.0"), 20099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.2-RELEASE"), 20099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.2.0-RELEASE"), 20099L);
		Assertions.assertEquals(ValueUtils.toVersion(20099L), "0.2.0-RELEASE");
		// 0.10, 0.10.0, 0.10-RELEASE, 0.10.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("0.10"), 100099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.10.0"), 100099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.10-RELEASE"), 100099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("0.10.0-RELEASE"), 100099L);
		Assertions.assertEquals(ValueUtils.toVersion(100099L), "0.10.0-RELEASE");

		// 1.0.0, 1.0.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("1.0.0"), 1000099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("1.0.0-RELEASE"), 1000099L);
		Assertions.assertEquals(ValueUtils.toVersion(1000099L), "1.0.0-RELEASE");
		// 2.0.0, 2.0.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("2.0.0"), 2000099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("2.0.0-RELEASE"), 2000099L);
		Assertions.assertEquals(ValueUtils.toVersion(2000099L), "2.0.0-RELEASE");
		// 10.0.0, 10.0.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("10.0.0"), 10000099L);
		Assertions.assertEquals(ValueUtils.toVersionNo("10.0.0-RELEASE"), 10000099L);
		Assertions.assertEquals(ValueUtils.toVersion(10000099L), "10.0.0-RELEASE");

		// 1.0.0, 1.0.0-RELEASE
		Assertions.assertEquals(ValueUtils.toVersionNo("1.0.0-RC1"), 1000001L);
		Assertions.assertEquals(ValueUtils.toVersion(1000001L), "1.0.0-RC1");
		Assertions.assertEquals(ValueUtils.toVersionNo("1.0.0-RC2"), 1000002L);
		Assertions.assertEquals(ValueUtils.toVersion(1000002L), "1.0.0-RC2");
	}

	protected static void printVersion(String version) {
		long toVersionNo = ValueUtils.toVersionNo(version);
		String toVersion = ValueUtils.toVersion(toVersionNo);
		log.info("version: " + version);
		log.info("toVersionNo: " + toVersionNo);
		log.info("toVersion: " + toVersion);
	}

	@Test
	public void testToString() {
		Assertions.assertEquals(ValueUtils.toString("a", "b"), "a");
		Assertions.assertEquals(ValueUtils.toString(null, "b"), "b");
		Assertions.assertEquals(ValueUtils.toString("", "b"), "b");
		Assertions.assertEquals(ValueUtils.toString(""), "");
		Assertions.assertEquals(ValueUtils.toString("", null), "");
		Assertions.assertEquals(ValueUtils.toString(null, ""), "");
		Assertions.assertEquals(ValueUtils.toString("", null, "", null), "");
		Assertions.assertEquals(ValueUtils.toString(null, null, "", null), "");
		Assertions.assertEquals(ValueUtils.toString("", null, "c", null), "c");
		Assertions.assertEquals(ValueUtils.toString(null, null, "c", null), "c");
		Assertions.assertEquals(ValueUtils.toString(null, "b", "c", null), "b");
		Assertions.assertEquals(ValueUtils.toString(null, null, "", "d"), "d");
		Assertions.assertEquals(ValueUtils.toString("", null, "", "d"), "d");
	}

	@Test
	public void testToRequiredType() {
		Assertions.assertArrayEquals(ValueUtils.toRequiredType("test", String[].class), new String[] { "test" });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(1, String[].class), new String[] { "1" });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(new String[] { "test1", "test2" }, String[].class), new String[] { "test1", "test2" });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(new Object[] { "test1", "test2" }, String[].class), new String[] { "test1", "test2" });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(new Object[] { "test1", "test2" }, Object[].class), new Object[] { "test1", "test2" });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(new Object[] { 1, 2 }, Object[].class), new Object[] { 1, 2 });
		Assertions.assertArrayEquals(ValueUtils.toRequiredType(new Object[] { 1, 2 }, String[].class), new String[] { "1", "2" });

		Assertions.assertEquals(ValueUtils.toRequiredType("MONOLITHIC", AppType.class), AppType.MONOLITHIC);
		Assertions.assertEquals(ValueUtils.toRequiredType("MSA", AppType.class), AppType.MSA);
	}

	@Test
	public void testMap() {
		A a, a2, a3 = null;

		a = new A();
		a.setX("l");
		a.setY("m");
		a.setZ(EquipmentStatus.AVAILABLE);
		a.setZs(Arrays.asList(EquipmentStatus.AVAILABLE));

		{
			a2 = ValueUtils.map(a, A.class);

			Assertions.assertEquals(a, a2);

			a3 = ValueUtils.map(a, A.class);
			a3.setY("n");

			Assertions.assertEquals(a, a2);
			Assertions.assertNotEquals(a2, a3);
			Assertions.assertEquals(a.getZs().get(0), a3.getZs().get(0));
			Assertions.assertEquals(a3.getZs().get(0), EquipmentStatus.AVAILABLE);
		}

		{
			a2 = ValueUtils.map2(a, A.class);

			Assertions.assertEquals(a, a2);

			a3 = ValueUtils.map2(a, A.class);
			a3.setY("n");

			Assertions.assertEquals(a, a2);
			Assertions.assertNotEquals(a2, a3);
			Assertions.assertEquals(a.getZs().get(0), a3.getZs().get(0));
			Assertions.assertEquals(a3.getZs().get(0), EquipmentStatus.AVAILABLE);
		}

	}

	@Data
	public static class A {
		private String x;
		private String y;
		private EquipmentStatus z;
		private List<EquipmentStatus> zs;
	}

}
