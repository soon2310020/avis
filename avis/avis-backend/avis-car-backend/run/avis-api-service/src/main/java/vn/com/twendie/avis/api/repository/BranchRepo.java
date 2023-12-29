package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchRepo extends JpaRepository<Branch, Long> {

    Optional<Branch> findByCode(String code);

    List<Branch> findAllByDeletedFalse();

    Optional<Branch> findByIdAndDeletedFalse(Long id);
}
