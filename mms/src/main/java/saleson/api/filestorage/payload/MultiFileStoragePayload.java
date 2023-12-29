package saleson.api.filestorage.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.StorageType;
import saleson.model.QFileStorage;

import java.util.List;


@Getter @Setter
@NoArgsConstructor
public class MultiFileStoragePayload extends EquipmentPayload {
    private List<StorageType> storageTypes;

    private Long refId;
    private Long refId2;

    public Predicate getPredicate() {
        QFileStorage fileStorage = QFileStorage.fileStorage;

        BooleanBuilder builder = new BooleanBuilder();


        if (getStorageTypes() != null) {
            builder.and(fileStorage.storageType.in(getStorageTypes()));
        }

        if (getRefId() != null) {
            builder.and(fileStorage.refId.eq(getRefId()));
        }

        if (getRefId2() != null) {
            builder.and(fileStorage.refId2.eq(getRefId2()));
        }
        return builder;

    }

}
