package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.response.UserDepartmentDTO;
import vn.com.twendie.avis.api.repository.UserDepartmentRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.UserDepartmentService;
import vn.com.twendie.avis.data.model.Department;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.List;
import java.util.Objects;

@Service
public class UserDepartmentServiceImpl implements UserDepartmentService {

    private final UserDepartmentRepo userDepartmentRepo;
    private final ListUtils listUtils;

    public UserDepartmentServiceImpl(UserDepartmentRepo userDepartmentRepo,
                                     ListUtils listUtils) {
        this.userDepartmentRepo = userDepartmentRepo;
        this.listUtils = listUtils;
    }

    @Override
    public List<UserDepartmentDTO> findAll() {
        return listUtils.mapAll(userDepartmentRepo.findAllByDeletedFalse(), UserDepartmentDTO.class);
    }

    @Override
    @Cacheable(cacheNames = "Department", key = "#id")
    public Department findById(Long id) {

        if (Objects.isNull(id)) {
            return null;
        }

        return userDepartmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot found department with id: " + id)
                        .displayMessage(Translator.toLocale("department.error.not_found")));
    }
}
