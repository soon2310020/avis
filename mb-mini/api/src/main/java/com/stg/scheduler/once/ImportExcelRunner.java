package com.stg.scheduler.once;

import com.stg.config.properties.ImportProperties;
import com.stg.service.BackOfficeService;
import com.stg.service.caching.RmExcelDataCaching;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportExcelRunner implements CommandLineRunner {
    private final ImportProperties importProperties;
    private final BackOfficeService backOfficeService;

    private final RmExcelDataCaching rmExcelDataCaching;

    @Override
    public void run(String... args) {
        // 1. import danh sach RM
        doImportRM();

    }


    /***/
    private void doImportRM() {
        if (rmExcelDataCaching.exists() && !importProperties.isRmRefresh()) {
            log.info("[ImportRmInfoRunner] Don't re-import!");
            return;
        }

        String fileName = "danh_sach_rm.xlsx";
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classloader.getResourceAsStream("excel/" + fileName);

            backOfficeService.readXlsxAndImportRmInfo(inputStream, fileName);
        } catch (Exception ex) {
            log.error("[AutoImportRmInfoRunner] Error when read file " + fileName, ex);
        }
    }
}
