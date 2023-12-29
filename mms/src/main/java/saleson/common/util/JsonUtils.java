package saleson.common.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

public class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER= new ObjectMapper();

    public static JavaType constructCollectionType(Class<? extends Collection<?>> collectionClass,
                                                   Class<?> elementType) {
        return OBJECT_MAPPER.getTypeFactory().constructCollectionType(collectionClass, elementType);
    }
    public static <E, T extends Collection<E>> T toCollection(String json, Class<T> collectionType, Class<E> elementType)
            throws IOException {
        try {
            return OBJECT_MAPPER.readValue(json, constructCollectionType(collectionType, elementType));
        } catch (Exception e) {
            throw e;
        }
    }

    public static String fileToString(String path) throws IOException {
        try (InputStream is = new ClassPathResource(path).getInputStream())
        {
            return IOUtils.toString(is, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }
    public static <T> T fromJson(String json, Class<T> type) throws IOException {
        try {
            return OBJECT_MAPPER.<T>readValue(json, type);
        } catch (Exception e) {
            throw e;
        }
    }

}
