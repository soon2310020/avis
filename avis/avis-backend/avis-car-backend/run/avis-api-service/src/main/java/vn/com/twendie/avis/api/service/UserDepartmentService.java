package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.response.UserDepartmentDTO;
import vn.com.twendie.avis.data.model.Department;

import java.util.List;

public interface UserDepartmentService {

    List<UserDepartmentDTO> findAll();

    Department findById(Long id);
}
