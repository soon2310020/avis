package vn.com.twendie.avis.mobile.api.pool;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.integration.util.SimplePool.PoolItemCallback;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class FTPClientPoolItemCallBack implements PoolItemCallback<FTPClient> {

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    @Override
    public FTPClient createForPool() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                if (ftpClient.login(username, password)) {
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    return ftpClient;
                } else {
                    log.error("Error when login to FTP server, reply code: {}", ftpClient.getReplyCode());
                }
            } else {
                log.error("Error when connect to FTP server, reply code: {}", ftpClient.getReplyCode());
            }
            ftpClient.disconnect();
        } catch (IOException e) {
            log.error("Error when create FTPClient {}", ExceptionUtils.getRootCauseMessage(e));
        }
        log.error("Failed to create FTPClient");
        return null;
    }

    @Override
    public boolean isStale(FTPClient ftpClient) {
        try {
            return !FTPReply.isPositiveCompletion(ftpClient.pwd());
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public void removedFromPool(FTPClient ftpClient) {
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            log.error("Error when disconnect FTPClient {}", ExceptionUtils.getRootCauseMessage(e));
        }
    }

}
