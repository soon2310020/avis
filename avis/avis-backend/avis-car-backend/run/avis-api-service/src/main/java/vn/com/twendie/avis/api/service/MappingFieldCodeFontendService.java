package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.MappingFieldCodeFontend;

import java.util.List;

public interface MappingFieldCodeFontendService {

    List<MappingFieldCodeFontend> findAll();

    MappingFieldCodeFontend findByTableAndField(String tableName, String fieldName);

}
