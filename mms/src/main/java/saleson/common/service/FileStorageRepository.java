package saleson.common.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.StorageType;
import saleson.model.FileStorage;

import java.util.List;

public interface FileStorageRepository extends JpaRepository<FileStorage, Long>, QuerydslPredicateExecutor<FileStorage> {
    List<FileStorage> findByRefId2(Long refId2);

    List<FileStorage> findByRefIdAndStorageType(Long refId, StorageType storageType);
}
