package vn.com.twendie.avis.mobile.api.support;

import java.io.IOException;

public interface FtpService {
    String uploadFile(String path) throws IOException;
}
