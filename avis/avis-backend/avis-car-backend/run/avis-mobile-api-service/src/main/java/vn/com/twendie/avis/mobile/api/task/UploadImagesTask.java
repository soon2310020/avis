package vn.com.twendie.avis.mobile.api.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.mobile.api.support.FtpService;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
public class UploadImagesTask {

    private final String uploadFolder;

    private final FtpService ftpService;

    public UploadImagesTask(@Value("${spring.upload_folder}") String uploadFolder,
                            FtpService ftpService) {
        this.uploadFolder = uploadFolder;
        this.ftpService = ftpService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Scheduled(fixedDelay = 10000, initialDelay = 1000)
    public void uploadImagesTask() {
        uploadImagesFPT();
    }

    public void uploadImages() {
        log.info("uploadImages start");
        new Thread(() -> {
            uploadImagesFPT();
        }).start();
        log.info("uploadImages done");
    }
    private synchronized void uploadImagesFPT() {
        log.info("uploadImagesFPT start");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            File dir = new File(uploadFolder);
            if (dir.exists() && dir.isDirectory() && Objects.nonNull(dir.listFiles())) {
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        if (ftpService.uploadFile(file.getAbsolutePath()) != null) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
            log.error(ignored.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        stopWatch.stop();
        if(stopWatch.getTime()>0)
        log.info("uploadImagesFPT done duration {}", stopWatch.getTime() );

    }


}
