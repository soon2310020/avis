package saleson.api.systemNote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.util.SecurityUtils;
import saleson.dto.SystemNoteParam;
import saleson.model.QSystemNote;
import saleson.model.QSystemNoteRead;
import saleson.model.SystemNote;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemNotePayload {
    private PageType systemNoteFunction;
    private Long objectFunctionId;

    List<Long> objectFunctionIds;

    private boolean trashBin;

    private String message;
    private List<SystemNoteParam> systemNoteParamList;
    //extend for Disconnection alert
    private ObjectType objectType;

    private Long parentId;


    public SystemNote getModel(){
        SystemNote systemNote = new SystemNote();
        bindData(systemNote);
        return systemNote;
    }
    public SystemNote getModel(SystemNote systemNote){
        bindData(systemNote);
        return systemNote;
    }

    public void bindData(SystemNote systemNote) {
        systemNote.setMessage(message);
        systemNote.setSystemNoteFunction(systemNoteFunction);
        systemNote.setObjectFunctionId(objectFunctionId);
        systemNote.setSystemNoteParamList(systemNoteParamList);
        systemNote.setObjectType(objectType);
//        systemNote.setIsReply(isReply);
        systemNote.setParentId(parentId);
    }

    public List<SystemNote> getListModels() {
        List<SystemNote> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectFunctionIds)) {
            objectFunctionIds.forEach(id -> {
                SystemNote systemNote = new SystemNote();
                systemNote.setMessage(message);
                systemNote.setSystemNoteFunction(systemNoteFunction);
                systemNote.setObjectFunctionId(id);
                systemNote.setSystemNoteParamList(systemNoteParamList);
                systemNote.setObjectType(objectType);
                result.add(systemNote);
            });
        }
        return result;
    }
    public Predicate getPredicate() {
        QSystemNote systemNote = QSystemNote.systemNote;
        BooleanBuilder builder= new BooleanBuilder();
        if(systemNoteFunction!=null){
            builder.and(systemNote.systemNoteFunction.eq(systemNoteFunction));
        }
        if(objectFunctionId!=null){
            builder.and(systemNote.objectFunctionId.eq(objectFunctionId));
        }
        if(objectType!=null){
            builder.and(systemNote.objectType.eq(objectType));
        }
        builder.and(systemNote.deleted.eq(trashBin));

        //for detail
        if(parentId!=null)
            builder.and(systemNote.parentId.eq(parentId));
        else
            builder.and(systemNote.parentId.isNull());

        return builder;

    }

    public Predicate getUnreadPredicate() {
        QSystemNote systemNote = QSystemNote.systemNote;
        QSystemNote systemNoteParent = new QSystemNote("parent");
        QSystemNoteRead systemNoteRead = QSystemNoteRead.systemNoteRead;
        BooleanBuilder builder = new BooleanBuilder();
        if (systemNoteFunction != null) {
            builder.and(systemNote.systemNoteFunction.eq(systemNoteFunction));
        }
        if (objectFunctionId != null) {
            builder.and(systemNote.objectFunctionId.eq(objectFunctionId));
        }
        if (objectType != null) {
            builder.and(systemNote.objectType.eq(objectType));
        }
        builder.and(systemNote.deleted.eq(trashBin));

        builder.and(systemNote.parentId.isNull()
            .or(systemNote.parentId.in(JPAExpressions
            .select(systemNoteParent.id)
            .from(systemNoteParent)
            .where(systemNoteParent.deleted.eq(trashBin))))
        );

        builder.and(systemNote.id.notIn(JPAExpressions
            .select(systemNoteRead.systemNoteId)
            .from(systemNoteRead)
            .where(systemNoteRead.userId.eq(SecurityUtils.getUserId())
                .and(systemNoteRead.isRead.isTrue()))));

        if (parentId != null)
            builder.and(systemNote.parentId.eq(parentId).or(systemNote.id.eq(parentId)));


        return builder;

    }

}
