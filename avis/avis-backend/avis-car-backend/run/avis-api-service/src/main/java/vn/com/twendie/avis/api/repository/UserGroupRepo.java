package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.UserGroup;

import java.util.List;

public interface UserGroupRepo extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findByDeletedFalse();
}
