package com.emoldino.api.common.resource.base.note.dto;

import java.util.List;

import lombok.Data;
import saleson.dto.SystemNoteParam;

@Data
public class NoteIn {
	private String message;
	private List<SystemNoteParam> params;

	public void setSystemNoteParamList(List<SystemNoteParam> params) {
		this.setParams(params);
	}
}
