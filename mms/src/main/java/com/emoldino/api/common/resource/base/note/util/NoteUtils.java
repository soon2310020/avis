package com.emoldino.api.common.resource.base.note.util;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.systemNote.SystemNoteService;
import saleson.api.systemNote.payload.SystemNotePayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;

public class NoteUtils {

	public static void post(PageType pageType, Long id, NoteIn notein) {
		post(pageType, null, id, notein);
	}

	public static void post(PageType pageType, ObjectType objectType, Long id, NoteIn notein) {
		SystemNotePayload payload = new SystemNotePayload();
		payload.setSystemNoteFunction(pageType);
		payload.setObjectType(objectType);
		payload.setObjectFunctionId(id);
		payload.setMessage(notein.getMessage());
		payload.setSystemNoteParamList(notein.getParams());
		BeanUtils.get(SystemNoteService.class).create(payload);
	}

}
