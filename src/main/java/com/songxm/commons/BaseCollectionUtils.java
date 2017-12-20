package com.songxm.commons;
import com.google.common.base.Preconditions;
import org.springframework.util.ObjectUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
@SuppressWarnings("unchecked")
public class BaseCollectionUtils {
    public BaseCollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean contains(Collection<?> collection, Object obj) {
        return isEmpty(collection)?false:collection.stream().filter((item) -> {
            return ObjectUtils.nullSafeEquals(item, obj);
        }).findAny().isPresent();
    }

    public static boolean containsIgnoreCase(Collection<String> collection, String s) {
        return isEmpty(collection)?false:collection.stream().filter((item) -> {
            return item == null && s == null || item.equalsIgnoreCase(s);
        }).findAny().isPresent();
    }

    public static void add(List list, int index, Object ele) {
        Preconditions.checkNotNull(list, "list参数不能为null");
        if(index < list.size()) {
            list.set(index, ele);
        } else {
            if(index > list.size()) {
                IntStream.range(list.size(), index).forEach((i) -> {
                    list.add(i, (Object)null);
                });
            }

            list.add(ele);
        }
    }

    public static <T> void shuffleList(List<T> list) {
        int size = list.size();
        Random random = new Random();
        random.nextInt();

        for(int i = 0; i < size; ++i) {
            int change = i + random.nextInt(size - i);
            swap(list, i, change);
        }

    }

    private static <T> void swap(List<T> list, int i, int change) {
        T temp = list.get(i);
        list.set(i, list.get(change));
        list.set(change,  temp);
    }
}
