package saleson.api.versioning.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import saleson.common.enumeration.RevisionObjectType;
import saleson.model.clone.RevisionHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReversionHistoryRepository extends JpaRepository<RevisionHistory, Long> {

    @Query(nativeQuery = true, value = "select * from REVISION_HISTORY as re where re.ORIGIN_ID = ?1 and re.REVISION_OBJECT_TYPE = ?2 order by ID desc", countQuery = "select count(*)")
    Page<RevisionHistory> getRevision(Long originId, String revisionObjectType, Pageable pageable);

    @Query(nativeQuery = true, value = "select count(*) from REVISION_HISTORY as re where re.ORIGIN_ID = ?1 and re.REVISION_OBJECT_TYPE = ?2")
    int getTotalElements(Long originId, String revisionObjectType);

    @Query(nativeQuery = true, value = "select * from REVISION_HISTORY as re where re.id <= ?1 and re.REVISION_OBJECT_TYPE = ?2 and re.ORIGIN_ID = ?3 order by id desc limit 2")
    List<RevisionHistory> getTop2RevisionHistories(Long id, String objectType, Long originId);

    Optional<List<RevisionHistory>> findByOriginIdAndRevisionObjectTypeAndCreatedAtBetweenOrderByCreatedAtAsc(Long originId, RevisionObjectType type, Instant start, Instant end);

    Optional<RevisionHistory> findFirstByOriginIdAndRevisionObjectTypeAndCreatedAtBeforeOrderByCreatedAtDesc(Long originId, RevisionObjectType type, Instant before);
    Optional<RevisionHistory> findFirstByOriginIdAndRevisionObjectTypeAndCreatedAtAfterOrderByCreatedAtAsc(Long originId, RevisionObjectType type, Instant before);

}
