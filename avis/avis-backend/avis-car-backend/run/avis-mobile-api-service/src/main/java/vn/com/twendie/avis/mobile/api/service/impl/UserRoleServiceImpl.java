package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.UserRole;
import vn.com.twendie.avis.mobile.api.repository.UserRoleRepo;
import vn.com.twendie.avis.mobile.api.service.UserRoleService;

import java.util.Objects;

@CacheConfig(cacheNames = "RoleService")
@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepo userRoleRepo;

    public UserRoleServiceImpl(UserRoleRepo userRoleRepo){
        this.userRoleRepo = userRoleRepo;
    }

    @Override
    @Cacheable(key = "#id")
    public UserRole findById(Long id) {

        if (Objects.isNull(id)) {
            return null;
        }

        return userRoleRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot find role with id: " + id));
    }

}
