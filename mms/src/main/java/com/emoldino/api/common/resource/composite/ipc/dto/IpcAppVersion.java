package com.emoldino.api.common.resource.composite.ipc.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IpcAppVersion {
	private String version;
	private String releaseDate;
	private boolean enabled = true;
	private List<IpcAppVersionItem> items;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IpcAppVersionItem {
		private String description;
	}
}
