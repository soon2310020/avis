package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.CreateIllustrationTableDto;
import com.stg.service.dto.insurance.IllustrationTableDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IllustrationTableService {

    PaginationResponse<IllustrationTableDto> list(Long user, int page, int size, String query, String packageName, String segment, String category);

    IllustrationTableDto illustrationTableDetail(Long userId, String illustrationId, String packageName);

    void exportList(Long idUser, String query, String type, String packageName, HttpServletResponse response, String segment, String category) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    void createIllustrationTable(String mbId, CreateIllustrationTableDto illustrationTableDto, Integer processId);

}
