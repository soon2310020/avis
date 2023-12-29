package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.UserGroup;
import vn.com.twendie.avis.mobile.api.model.response.UserGroupDTO;

import java.util.Objects;
import java.util.function.Function;

public class UserGroupDTOAdapter implements Function<UserGroup, UserGroupDTO> {

    @Override
    public UserGroupDTO apply(UserGroup userGroup) {
        if (Objects.isNull(userGroup)) {
            return null;
        }

        return UserGroupDTO.builder()
                .id(userGroup.getId())
                .code(userGroup.getCode())
                .name(userGroup.getName())
                .build();
    }
}
