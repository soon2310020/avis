package vn.com.twendie.avis.api.core.util;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class ListUtils {

    private final ModelMapper modelMapper;

    public ListUtils(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <F, T> List<T> transform(Collection<F> fromList, Function<? super F, ? extends T> function) {
        return fromList.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    public <F, T> List<T> mapAll(Collection<F> fromList, Class<T> clazz) {
        return transform(fromList, item -> modelMapper.map(item, clazz));
    }

    public <F, T> Page<T> transform(Page<F> fromPage, Function<? super F, ? extends T> function) {
        List<T> list = fromPage.get()
                .map(function)
                .collect(Collectors.toList());
        return new PageImpl<>(list, fromPage.getPageable(), fromPage.getTotalElements());
    }

    public <F, T> Page<T> mapAll(Page<F> fromPage, Class<T> clazz) {
        return transform(fromPage, item -> modelMapper.map(item, clazz));
    }

    public <T> List<T> filter(Collection<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    public <T> T findFirst(Collection<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).findFirst().orElse(null);
    }

    public <T> void addIfNotNull(List<T> list, T item) {
        if (Objects.nonNull(item)) {
            list.add(item);
        }
    }

    public <T> List<T> removeDuplicate(List<T> list) {
        List<T> newList = new ArrayList<>();
        list.forEach(item -> {
            if (newList.isEmpty() || !newList.get(newList.size() - 1).equals(item)) {
                newList.add(item);
            }
        });
        return newList;
    }

}
