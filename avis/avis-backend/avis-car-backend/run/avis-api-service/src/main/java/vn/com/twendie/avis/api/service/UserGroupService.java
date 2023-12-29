package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.UserGroup;

import java.util.List;

public interface UserGroupService {

    UserGroup findById(Long id);

    List<UserGroup> findByDeletedFalse();
}
