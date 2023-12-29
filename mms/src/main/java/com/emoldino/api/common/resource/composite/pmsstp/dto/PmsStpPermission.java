package com.emoldino.api.common.resource.composite.pmsstp.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class PmsStpPermission extends PmsStpResource {
	private Boolean permitted;
	private Boolean editable;
	private List<PmsStpItem> items;
	private List<PmsStpPermission> subpermissions;

	public void addItem(PmsStpItem item) {
		if (this.items == null) {
			this.items = new ArrayList<>();
		}
		this.items.add(item);
	}

	public void addSubpermission(PmsStpPermission subpermission) {
		if (this.subpermissions == null) {
			this.subpermissions = new ArrayList<>();
		}
		this.subpermissions.add(subpermission);
	}
}
