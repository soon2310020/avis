package com.emoldino.api.common.resource.composite.usrstp.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.QRoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUserRepository;
import com.emoldino.api.common.resource.base.accesscontrol.service.role.RoleAccessControlService;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpData;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpData.UsrStpPlant;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpGetPlantsOut;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpRole;
import com.emoldino.api.common.resource.composite.usrstp.enumeration.UsrStpAccessLevel;
import com.emoldino.api.common.resource.composite.usrstp.enumeration.UsrStpStatus;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.location.LocationRepository;
import saleson.api.user.UserController;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserPayload;
import saleson.common.enumeration.RoleUserData;
import saleson.model.QLocation;
import saleson.model.User;

@Service
public class UsrStpService {

	public UsrStpData get(Long id) {
		User user = BeanUtils.get(UserService.class).findById(id);
		if (user == null) {
			return null;
		}

		UsrStpData data = ValueUtils.map(user, UsrStpData.class);
		data.setPassword(null);
		data.setSsoEnabled(!ObjectUtils.isEmpty(user.getSsoId()));

		if (user.isEnabled()) {
			data.setStatus(UsrStpStatus.ENABLED);
		} else if (ValueUtils.toBoolean(user.getRequested(), false)) {
			data.setStatus(UsrStpStatus.REQUESTED);
		} else {
			data.setStatus(UsrStpStatus.DISABLED);
		}

		if (user.isAdmin()) {
			data.setAccessLevel(UsrStpAccessLevel.ADMIN);
		} else if (RoleUserData.ROLE_REST_USER.equals(user.getRoleUserData())) {
			data.setAccessLevel(UsrStpAccessLevel.REST);
		} else {
			data.setAccessLevel(UsrStpAccessLevel.REGULAR);
		}

		populatePlants(data);

		return data;
	}

	public UsrStpGetPlantsOut getPlants(Long userId, Long companyId) {
		UsrStpData data = new UsrStpData();
		data.setId(userId);
		data.setCompanyId(companyId);
		populatePlants(data);
		UsrStpGetPlantsOut output = new UsrStpGetPlantsOut();
		output.setContent(data.getPlants());
		output.setAvailableRoles(data.getAvailableRoles());
		return output;
	}

	public ListOut<UsrStpRole> getRoles(Long companyId) {
		ValueUtils.assertNotEmpty(companyId, "companyId");
		RoleGetIn reqin = new RoleGetIn();
		reqin.setCompanyId(companyId);
		reqin.setEnabled(true);
		ListOut<UsrStpRole> output = new ListOut<>();
		output.setContent(BeanUtils.get(RoleAccessControlService.class).getPage(reqin, PageRequest.of(0, 1000)).map(role -> ValueUtils.map(role, UsrStpRole.class)).getContent());
		return output;
	}

	public void post(UsrStpData data) {
		UserPayload payload = toUserPayload(data);
		BeanUtils.get(UserController.class).post(payload);
		if (ObjectUtils.isEmpty(data.getPlants())) {
			return;
		}
		User user = BeanUtils.get(UserRepository.class)//
				.findByEmailAndDeletedIsFalse(data.getEmail())//
				.orElse(null);
		if (user == null) {
			return;
		}
		savePlants(user.getId(), data.getPlants());
	}

	public void put(Long id, UsrStpData data) {
		UserPayload payload = toUserPayload(data);
		BeanUtils.get(UserController.class).put(id, payload);
		savePlants(id, data.getPlants());
	}

	private static UserPayload toUserPayload(UsrStpData data) {
		UserPayload payload = ValueUtils.map(data, UserPayload.class);

		if (UsrStpStatus.ENABLED.equals(data.getStatus())) {
			payload.setEnabled(true);
			payload.setRequested(false);
		} else if (UsrStpStatus.REQUESTED.equals(data.getStatus())) {
			payload.setEnabled(false);
			payload.setRequested(true);
		} else {
			payload.setEnabled(false);
			payload.setRequested(false);
		}

		if (UsrStpAccessLevel.ADMIN.equals(data.getAccessLevel())) {
			payload.setAdmin(true);
			payload.setRoleUserData(RoleUserData.ROLE_WEB_USER);
		} else if (UsrStpAccessLevel.REST.equals(data.getAccessLevel())) {
			payload.setAdmin(false);
			payload.setRoleUserData(RoleUserData.ROLE_REST_USER);
		} else {
			payload.setAdmin(false);
			payload.setRoleUserData(RoleUserData.ROLE_WEB_USER);
		}

		return payload;
	}

	private void populatePlants(UsrStpData data) {
		if (data.getCompanyId() == null) {
			return;
		}

		Map<Long, UsrStpRole> roles = new LinkedHashMap<>();
		getRoles(data.getCompanyId()).getContent().forEach(role -> roles.put(role.getId(), ValueUtils.map(role, UsrStpRole.class)));
		if (roles.isEmpty()) {
			return;
		}
		data.setAvailableRoles(new ArrayList<>(roles.values()));

		Map<Long, UsrStpPlant> plants = new LinkedHashMap<>();
		BeanUtils.get(LocationRepository.class).findAll(//
				new BooleanBuilder().and(QLocation.location.companyId.eq(data.getCompanyId())), //
				Sort.by(Direction.ASC, "name")//
		).forEach(location -> plants.put(location.getId(), ValueUtils.map(location, UsrStpPlant.class)));
		if (plants.isEmpty()) {
			return;
		}

		if (data.getId() != null) {
			BeanUtils.get(RoleUserRepository.class).findAll(new BooleanBuilder().and(QRoleUser.roleUser.userId.eq(data.getId())), Sort.by(Direction.ASC, "createdAt"))//
					.forEach(roleUser -> {
						if (!plants.containsKey(roleUser.getLocationId()) || !roles.containsKey(roleUser.getRoleId())) {
							return;
						}
						UsrStpPlant plant = plants.get(roleUser.getLocationId());
						plant.getRoles().add(roles.get(roleUser.getRoleId()));
					});
		}

		data.setPlants(new ArrayList<>(plants.values()));
	}

	private void savePlants(Long id, List<UsrStpPlant> plants) {
		QRoleUser table = QRoleUser.roleUser;
		if (ObjectUtils.isEmpty(plants)) {
			BeanUtils.get(RoleUserRepository.class).deleteAll(//
					BeanUtils.get(RoleUserRepository.class).findAll(new BooleanBuilder().and(table.userId.eq(id)))//
			);
			return;
		}

		List<RoleUser> oldList = new ArrayList<>();
		BeanUtils.get(RoleUserRepository.class).findAll(new BooleanBuilder().and(table.userId.eq(id))).forEach(item -> oldList.add(item));

		boolean same = true;
		List<RoleUser> newList = new ArrayList<>();
		for (UsrStpPlant plant : plants) {
			if (ObjectUtils.isEmpty(plant.getRoles())) {
				for (RoleUser item : oldList) {
					if (item.getLocationId().equals(plant.getId())) {
						same = false;
						break;
					}
				}
				continue;
			}
			for (UsrStpRole role : plant.getRoles()) {
				if (oldList.isEmpty()) {
					same = false;
				} else if (same) {
					RoleUser roleUser = oldList.get(0);
					if (roleUser.getLocationId().equals(plant.getId()) && roleUser.getRoleId().equals(role.getId())) {
						oldList.remove(0);
						newList.add(roleUser);
						continue;
					} else {
						same = false;
					}
				}

				RoleUser roleUser = new RoleUser();
				roleUser.setLocationId(plant.getId());
				roleUser.setRoleId(role.getId());
				roleUser.setUserId(id);
				newList.add(roleUser);
			}
		}
		if (same) {
			return;
		}

		BeanUtils.get(RoleUserRepository.class).deleteAll(oldList);
		BeanUtils.get(RoleUserRepository.class).saveAll(newList);
	}

}
