package com.emoldino.framework.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.emoldino.framework.util.ValueUtils;

public abstract class AbstractTool {
	private static final Scanner scanner = new Scanner(System.in);

	protected static String scan(String message) {
		System.out.print(message + ": ");
		String value = scanner.next().trim();
		return "exit".equalsIgnoreCase(value) ? null : value;
	}

	protected static List<String> scanAll(String resourceType) {
		List<String> resources = new ArrayList<>();
		do {
			String resource = scan(resourceType);
			if (resource == null) {
				return null;
			}

			if ("n".equalsIgnoreCase(resource) || "all".equalsIgnoreCase(resource)) {
				break;
			}

			if (resources.contains(resource)) {
				continue;
			}

			resources.add(resource);
			if (!ValueUtils.toBoolean(scan("more " + resourceType + " exists?"), false)) {
				break;
			}
		} while (true);

		return resources;
	}
}
