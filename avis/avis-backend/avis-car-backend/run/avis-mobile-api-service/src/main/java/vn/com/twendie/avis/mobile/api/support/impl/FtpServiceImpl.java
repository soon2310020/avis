package vn.com.twendie.avis.mobile.api.support.impl;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.integration.util.Pool;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.mobile.api.config.FtpConfig;
import vn.com.twendie.avis.mobile.api.support.FtpService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@Service
public class FtpServiceImpl implements FtpService {

    private final FtpConfig ftpConfig;

    private final Pool<FTPClient> ftpClientPool;

    public FtpServiceImpl(FtpConfig ftpConfig,
                          Pool<FTPClient> ftpClientPool) {
        this.ftpConfig = ftpConfig;
        this.ftpClientPool = ftpClientPool;
    }

    @Override
    public String uploadFile(String path) throws IOException {

        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        FTPClient ftpClient = null;
        try {
            ftpClient = ftpClientPool.getItem();
            if (ftpClient.storeFile(file.getName(), fileInputStream)) {
                return "ftp://" + ftpConfig.getHost() + '/' + file.getName();
            }
        }
        finally {
            if (Objects.nonNull(ftpClient)) {
                ftpClientPool.releaseItem(ftpClient);
            }
        }
        return null;
    }
}
