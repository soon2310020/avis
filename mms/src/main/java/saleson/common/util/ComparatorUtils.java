package saleson.common.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

public class ComparatorUtils {
    public static Map<String, Long> sortByValue(Map<String, Long> unsortMap, int pageSize, int pageNumber, Sort.Direction direction) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Long>> list =
                new LinkedList<Map.Entry<String, Long>>(unsortMap.entrySet());



        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        if(direction.equals(Sort.Direction.DESC)){
            Collections.reverse(list);
        }

        if(list.size() > (pageNumber + 1) * pageSize) {
            list = list.subList(pageNumber * pageSize, (pageNumber + 1) * pageSize);
        }else{
            list = list.subList(pageNumber * pageSize, list.size());
        }
        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
