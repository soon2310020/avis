package com.stg.controller.aspect;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

public class CachedHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedPayload;
    private ServletInputStream inputStream; 
    private BufferedReader reader;
    
    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedPayload = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new CachedServletInputStream(this.cachedPayload);
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() {
        if (reader == null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedPayload);
            reader = new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }
        return reader;
    }
}