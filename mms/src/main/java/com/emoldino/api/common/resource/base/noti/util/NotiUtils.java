package com.emoldino.api.common.resource.base.noti.util;

import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn.NotiPostRecipient;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiRecipientType;
import com.emoldino.api.common.resource.base.noti.service.NotiService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;

public class NotiUtils {

	public static void post(NotiCode notiCode, NotiPostIn input) {
		if (input == null) {
			input = new NotiPostIn();
		}

		if (ConfigUtils.isProdMode()) {
			BeanUtils.get(NotiService.class).enqueue(notiCode, input);
		} else {
			BeanUtils.get(NotiService.class).post(notiCode, input);
		}
	}

	public static NotiPostRecipient toRecipientByEmail(String email) {
		NotiPostRecipient recp = new NotiPostRecipient();
		recp.setRecipientType(NotiRecipientType.USER);
		recp.setValue(email);
		return recp;
	}

	public static NotiPostRecipient toRecipientByRole(String roleCode) {
		NotiPostRecipient recp = new NotiPostRecipient();
		recp.setRecipientType(NotiRecipientType.ROLE);
		recp.setValue(roleCode);
		return recp;
	}

	public static NotiPostRecipient getEmoldinoAdminRecipient() {
		return NotiPostRecipient.builder().recipientType(NotiRecipientType.EMOLDINO_ADMIN).build();
	}

	public static NotiPostRecipient getOemAdminRecipient() {
		return NotiPostRecipient.builder().recipientType(NotiRecipientType.OEM_ADMIN).build();
	}

	public static NotiPostRecipient getSupplierAdminRecipient() {
		return NotiPostRecipient.builder().recipientType(NotiRecipientType.SUPPLIER_ADMIN).build();
	}

	public static NotiCategory toCategory(NotiCode notiCode) {
		NotiCategory notiCate;
		String name = notiCode.name();
		if (name.startsWith("WO_") || name.startsWith("CM_") || name.startsWith("PM_")) {
			notiCate = NotiCategory.WORK_ORDER;
		} else if (name.startsWith("NOTE_")) {
			notiCate = NotiCategory.NOTE;
		} else if (name.startsWith("DR_")) {
			notiCate = NotiCategory.DATA_REQUEST;
		} else if (name.startsWith("USER_ACCESS_")) {
			notiCate = NotiCategory.USER_ACCESS;
		} else if (name.startsWith("TOOLING_") || name.startsWith("MACHINE_") || name.startsWith("TERMINAL_") || name.startsWith("SENSOR_")) {
			notiCate = NotiCategory.ALERT;
		} else {
			notiCate = NotiCategory.OTHERS;
		}
		return notiCate;
	}

}
