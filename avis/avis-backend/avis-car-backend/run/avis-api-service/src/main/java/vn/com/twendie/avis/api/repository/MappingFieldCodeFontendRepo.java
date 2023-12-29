package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.MappingFieldCodeFontend;

import java.util.Optional;

public interface MappingFieldCodeFontendRepo extends JpaRepository<MappingFieldCodeFontend, Long> {

    Optional<MappingFieldCodeFontend> findByTableNameAndFieldNameAndDeletedFalse(String tableName, String fieldName);

}
