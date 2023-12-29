package com.emoldino.api.common.resource.composite.manpag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManPagLoginIn {
	private String sessionId;
	private String username;
	private String clientId;
	private String messagingToken;
}
