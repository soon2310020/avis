package vn.com.twendie.avis.api.core.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonUtils {

    @Getter
    private final ObjectMapper mapper;

    public JsonUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JavaType constructJavaType(Class clazz) {
        return mapper.constructType(clazz);
    }

    public JavaType constructJavaType(Class<?> clazz, Class<?>... genericClasses) {
        return mapper.getTypeFactory().constructParametricType(clazz, genericClasses);
    }

    public JsonNode readFile(String filePath) throws IOException {
        return readFile(filePath, JsonNode.class);
    }

    public <T> T readFile(String filePath, Class<T> clazz) throws IOException {
        return readFile(filePath, mapper.constructType(clazz));
    }

    public <T> T readFile(String filePath, JavaType javaType) throws IOException {
        InputStream inputStream = JsonUtils.class.getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return readThenClose(inputStream, javaType);
    }

    public <T> T readThenClose(InputStream inputStream, Class<T> clazz) throws IOException {
        try {
            return mapper.readValue(inputStream, clazz);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public <T> T readThenClose(InputStream inputStream, JavaType javaType) throws IOException {
        try {
            return mapper.readValue(inputStream, javaType);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public String toJson(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public <T> T toObject(String content, Class<T> clazz) throws IOException {
        return mapper.readValue(content, clazz);
    }

    public <T> T toObject(String content, JavaType javaType) throws IOException {
        return mapper.readValue(content, javaType);
    }

}
