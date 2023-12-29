package saleson.api.filestorage.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.StorageType;
import saleson.model.QFileStorage;


@Getter @Setter
@NoArgsConstructor
public class FileStoragePayload extends EquipmentPayload {
	private StorageType storageType;

	private Long refId;
	private Long refId2;

	public Predicate getPredicate() {
		QFileStorage fileStorage = QFileStorage.fileStorage;

		BooleanBuilder builder = new BooleanBuilder();


		if (getStorageType() != null) {
			builder.and(fileStorage.storageType.eq(getStorageType()));
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
