package vn.com.twendie.avis.api.service.impl;

import org.springframework.web.multipart.MultipartFile;

public interface ImportExcelService {
    String saveFile(MultipartFile file) throws Exception;

    void importVehicleExcel(MultipartFile file) throws Exception;
}
