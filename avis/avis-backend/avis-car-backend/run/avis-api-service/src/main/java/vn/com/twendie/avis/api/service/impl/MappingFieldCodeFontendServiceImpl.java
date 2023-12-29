package vn.com.twendie.avis.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.MappingFieldCodeFontendRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.MappingFieldCodeFontendService;
import vn.com.twendie.avis.data.model.MappingFieldCodeFontend;

import java.util.List;

@Service
@CacheConfig(cacheNames = "MappingFieldCodeFontend")
public class MappingFieldCodeFontendServiceImpl implements MappingFieldCodeFontendService {

    private final MappingFieldCodeFontendRepo mappingFieldCodeFontendRepo;

    private final MappingFieldCodeFontendService mappingFieldCodeFontendService;

    public MappingFieldCodeFontendServiceImpl(MappingFieldCodeFontendRepo mappingFieldCodeFontendRepo,
                                              @Lazy MappingFieldCodeFontendService mappingFieldCodeFontendService) {
        this.mappingFieldCodeFontendRepo = mappingFieldCodeFontendRepo;
        this.mappingFieldCodeFontendService = mappingFieldCodeFontendService;
    }

    @Override
    @Cacheable(key = "'all'")
    public List<MappingFieldCodeFontend> findAll() {
        return mappingFieldCodeFontendRepo.findAll();
    }

    @Override
    public MappingFieldCodeFontend findByTableAndField(String tableName, String fieldName) {
        return mappingFieldCodeFontendService.findAll().stream()
                .filter(mapping -> mapping.getTableName().equals(tableName) && mapping.getFieldName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("Not found mapping_field_code_fontend with table_name: %s, filed_name: %s", tableName, fieldName)));
    }

}
