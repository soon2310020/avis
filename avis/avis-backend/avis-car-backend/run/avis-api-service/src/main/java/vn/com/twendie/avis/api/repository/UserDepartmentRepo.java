package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.Department;

import java.util.List;

public interface UserDepartmentRepo extends JpaRepository<Department, Long> {

    List<Department> findAllByDeletedFalse();
}
