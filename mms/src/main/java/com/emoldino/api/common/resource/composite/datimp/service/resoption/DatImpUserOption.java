package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.user.UserController;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;
import saleson.model.User;

@Getter
@NoArgsConstructor
public class DatImpUserOption implements ResOptionGetter<UserPayload> {
	private DatImpResourceType resourceType = DatImpResourceType.USER;
	private ResOption<UserPayload> resOption = new ResOption<UserPayload>(//
			// 1. Sheet Name
			Arrays.asList("User"), //
			// 2. Object Type
			ObjectType.USER, //
			// 3. Item Class
			UserPayload.class, //
			// 4. Code Field
			Arrays.asList("email"),
			// 5. Populate
			item -> populate(item),
			// 6. Before Logic
			item -> doBefore(item), //
			// 7. Exists Logic
			item -> exists(item), //
			// 8. Post Logic
			item -> post(item), //
			// 9. Put Logic
			item -> put(item)//
	);

	private void populate(UserPayload item) {
		if (ObjectUtils.isEmpty(item.getEmail())) {
			return;
		}
		User user = BeanUtils.get(UserRepository.class)//
				.findByEmailAndDeletedIsFalse(item.getEmail())//
				.orElse(null);
		if (user == null) {
			return;
		}
		ValueUtils.map(user, item);
	}

	private void doBefore(UserPayload item) {
		item.setCompanyId(DatImpUtils.getCompanyId(item.getCompanyCode(), "companyCode", item.getCompanyId()));
		item.setValidImportCheck(true);
	}

	private boolean exists(UserPayload item) {
		return BeanUtils.get(UserService.class).existByLoginIdOrEmail(item.getEmail(), item.getEmail());
	}

	private void post(UserPayload item) {
		item.setEnabled(true);
		item.setRequested(false);
		if (ObjectUtils.isEmpty(item.getPassword())) {
			item.setPassword("changeit");
		}
		ApiResponse response = BeanUtils.get(UserController.class).post(item);
		if (response.getData() != null && response.getData() instanceof Long) {
			item.setId((Long) response.getData());//return id
		}
		DatImpUtils.response(response);
	}

	private void put(UserPayload item) {
		User user = BeanUtils.get(UserRepository.class)//
				.findByEmailAndDeletedIsFalse(item.getEmail())//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(User.class, DatImpUtils.toXlsColumnTitle("email"), item.getEmail()));
		item.setEnabled(user.isEnabled());
		ApiResponse response = BeanUtils.get(UserController.class).put(user.getId(), item);
		item.setId(user.getId());
		DatImpUtils.response(response);
	}
}
