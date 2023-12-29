package com.emoldino.api.common.resource.composite.dsh.dto;

import java.util.ArrayList;
import java.util.List;

import com.emoldino.framework.dto.ListIn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DshConfigsGetOut extends ListIn<DshWidget> {
	private List<DshWidget> contentEnabled = new ArrayList<>();

	public void addEnabled(DshWidget item) {
		contentEnabled.add(item);
	}
}
