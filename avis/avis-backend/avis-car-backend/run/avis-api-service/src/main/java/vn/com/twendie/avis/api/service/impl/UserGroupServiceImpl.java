package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.UserGroupRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.UserGroupService;
import vn.com.twendie.avis.data.model.UserGroup;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheNames = "UserGroupService")
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepo userGroupRepo;

    public UserGroupServiceImpl(UserGroupRepo userGroupRepo) {
        this.userGroupRepo = userGroupRepo;
    }

    @Override
    @Cacheable(key = "#id")
    public UserGroup findById(Long id) {

        if (Objects.isNull(id)) {
            return null;
        }

        return userGroupRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot find user group with id: " + id));
    }

    @Override
    public List<UserGroup> findByDeletedFalse() {
        return userGroupRepo.findByDeletedFalse();
    }
}
