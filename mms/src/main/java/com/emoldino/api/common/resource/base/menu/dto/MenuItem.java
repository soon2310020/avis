package com.emoldino.api.common.resource.base.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MenuItem {
	private String id;
	private String name;
	private String message;
	private String type;
	private String idRef;

	public MenuItem(String id, String name, String message, String type) {
		this(id, name, message, type, null);
	}
}
