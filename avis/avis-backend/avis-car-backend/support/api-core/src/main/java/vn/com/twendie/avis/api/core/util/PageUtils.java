package vn.com.twendie.avis.api.core.util;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

public class PageUtils {

    public static <T> PageImpl<T> toPage (int page, int size, List<T> totalItems) {
        int pageOffSet = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageOffSet, size);

        int total = totalItems.size();
        List<T> pageContents;
        int totalPage = (int) Math.ceil((double) total/size);
        if (page > totalPage) {
            pageContents = Collections.emptyList();
        } else {
            pageContents = totalItems.subList(pageOffSet * size,
                    Math.min((pageOffSet * size + size), total));
        }
        return new PageImpl<T>(pageContents, pageable, total);
    }
}
