package vn.com.twendie.avis.api.core.util;

import com.fasterxml.jackson.databind.JavaType;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class OkHttpUtils {

    private final JsonUtils jsonUtils;

    private final OkHttpClient okHttpClient;

    @Autowired
    public OkHttpUtils(JsonUtils jsonUtils,
                       OkHttpClient okHttpClient) {
        this.jsonUtils = jsonUtils;
        this.okHttpClient = okHttpClient;
    }

    public Response execute(Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    public Response execute(Request.Builder builder) throws IOException {
        return execute(builder.build());
    }

    public Builder createRequestBuilder() {
        return new Builder()
                .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .cacheControl(CacheControl.FORCE_NETWORK);
    }

    public Builder createRequestBuilder(String token) {
        return createRequestBuilder().addHeader(HttpHeaders.AUTHORIZATION, token);
    }

    public RequestBody createPostBody(Object object) throws IOException {
        return RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_UTF8_VALUE),
                jsonUtils.toJson(object));
    }

    public RequestBody createEmptyBody() {
        return RequestBody.create(null, new byte[0]);
    }

    public String getResponseAsString(Response response) throws IOException {
        try {
            return Objects.nonNull(response.body()) ? response.body().string() : null;
        } finally {
            IOUtils.closeQuietly(response.body());
        }
    }

    public <T> T getResponseAsObject(Response response, Class<T> clazz) throws Exception {
        return Objects.nonNull(response.body()) ? jsonUtils.readThenClose(response.body().byteStream(), clazz) : null;
    }

    public <T> T getResponseAsObject(Response response, JavaType javaType) throws Exception {
        return Objects.nonNull(response.body()) ? jsonUtils.readThenClose(response.body().byteStream(), javaType) : null;
    }

}
