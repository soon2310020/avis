package saleson.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtils {
    private ObjectMapper objectMapper;
/*

    private static ModelMapper modelMapper;
    public static ModelMapper getModelMapper(){
        if(modelMapper==null) modelMapper=new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);


        return modelMapper;
    }
*/
    public static <F, T> List<T> transform(Collection<F> fromList, Function<? super F, ? extends T> function) {
        return fromList == null ? null : fromList.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public static <F, T> List<T> mapAll(Collection<F> fromList, Class<T> clazz, ObjectMapper modelMapper) {
        return transform(fromList, item -> modelMapper.convertValue(item, clazz));
    }

    public static <E extends Enum<E>, T> List<T> mapAll(Class<E> enumType, Class<T> clazz, ObjectMapper modelMapper) {
        return mapAll(Lists.newArrayList(enumType.getEnumConstants()), clazz, modelMapper);
    }

    public static <T> List<T> subList(List<T> fromList, Integer pageIndex, Integer pageSize) {
        if (fromList != null && !fromList.isEmpty() && pageIndex!=null && pageSize!=null) {
            if (pageIndex * pageSize >= fromList.size()) return new ArrayList<>();
            if ((pageIndex + 1) * pageSize > fromList.size())
                return new ArrayList<>(fromList.subList(pageIndex * pageSize, fromList.size()));
            return new ArrayList<>(fromList.subList(pageIndex * pageSize, (pageIndex + 1) * pageSize));
        }
        return fromList;
    }

    static String fileToString(String path) throws IOException {
        try (InputStream is = new ClassPathResource(path).getInputStream())
        {
            return IOUtils.toString(is, Charset.forName("UTF-8"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public static  <E> List<E> parseJsonToList(String path, Class<E> entity) throws IOException {
        return JsonUtils.toCollection(fileToString(path), List.class, entity);
    }
    public static  <E> E parseJsonToEntity(String path, Class<E> entity) throws IOException {
        return JsonUtils.fromJson(fileToString(path), entity);
    }

}
