package com.stg.scheduler.once;

import java.io.InputStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.stg.config.properties.ImportProperties;
import com.stg.service.BackofficeService;
import com.stg.service.caching.RmExcelDataCaching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportExcelRunner implements CommandLineRunner {
    private final ImportProperties importProperties;
    private final BackofficeService backofficeService;

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

            backofficeService.readXlsxAndImportRmInfo(inputStream, fileName);
        } catch (Exception ex) {
            log.error("[ImportRmInfoRunner] Error when read file " + fileName, ex);
        }
    }

}
