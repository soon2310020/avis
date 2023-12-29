package com.emoldino.api.common.resource.composite.tmnstp;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetIn;
import com.emoldino.api.common.resource.composite.tmnstp.enumeration.TmnStpConnectionStatus;
import com.emoldino.framework.util.ValueUtils;

public class TmnStpTest {

	@Test
	public void testContainsEnum() {
		TmnStpGetIn input = new TmnStpGetIn();
		input.setConnectionStatus(Arrays.asList(TmnStpConnectionStatus.ONLINE));
		TmnStpGetIn inputCopy = ValueUtils.map(input, TmnStpGetIn.class);

		Assertions.assertTrue(inputCopy.getConnectionStatus().contains(TmnStpConnectionStatus.ONLINE));
		Assertions.assertFalse(inputCopy.getConnectionStatus().contains(TmnStpConnectionStatus.OFFLINE));
	}

}
