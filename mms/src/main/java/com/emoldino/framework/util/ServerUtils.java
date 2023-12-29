package com.emoldino.framework.util;

import java.net.*;

public class ServerUtils {

	public static String getName() {
		String name;
		try {
			name = InetAddress.getLocalHost().getHostName() + ":" + ConfigUtils.getHttpPort();
		} catch (UnknownHostException e) {
			name = ConfigUtils.getHttpPort() + "";
		}
		return name;
	}

}
