package vn.com.twendie.avis.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.Feedback;

public interface FeedbackRepo extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findByCreatedByIdAndDeletedFalseOrderByCreatedAtDesc(Long createdById, Pageable pageable);

}
