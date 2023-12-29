package saleson.api.systemNote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.PageType;
import org.springframework.data.repository.query.Param;
import saleson.model.SystemNote;

import java.util.List;

public interface SystemNoteRepository extends JpaRepository<SystemNote, Long>, QuerydslPredicateExecutor<SystemNote> {

    List<SystemNote> findAllBySystemNoteFunctionAndObjectFunctionIdAndDeleted(PageType function,Long objectFunctionId,boolean deleted);

    @Query("select CASE WHEN count(s) > 0 then true else false END from SystemNoteRead s where s.systemNoteId=:systemNoteId and s.userId=:userId")
    boolean isReadSystemNote(@Param("userId") Long userId,@Param("systemNoteId")  Long systemNoteId);

    @Query("select s from SystemNote s where s.id not in (select sr.systemNoteId from SystemNoteRead sr where sr.userId=:userId) and s.id in (:ids)")
    List<SystemNote> findAllUnread(@Param("userId") Long userId, @Param("ids") List<Long> ids);

}
