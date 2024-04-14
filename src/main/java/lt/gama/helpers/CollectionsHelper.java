package lt.gama.helpers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * gama-online
 * Created by valdas on 2018-03-04.
 */
public final class CollectionsHelper {

    private CollectionsHelper() {}

    public static boolean isEmpty(final Collection<?> a) {
        return a == null || a.isEmpty();
    }

    public static boolean hasValue(final Collection<?> a) {
        return !isEmpty(a);
    }

    public static boolean isEmpty(final Map<?, ?> a) {
        return a == null || a.isEmpty();
    }

    public static boolean hasValue(final Map<?, ?> a) {
        return !isEmpty(a);
    }

    private static <T> boolean isEqualCollection(final Collection<T> a, final Collection<T> b) {
        return a.containsAll(b) && b.containsAll(a);
    }

    public static <T> boolean isEqual(final Collection<T> a, final Collection<T> b) {
        return isEmpty(a) && isEmpty(b) ||
                !isEmpty(a) && !isEmpty(b) && isEqualCollection(a, b);
    }

    public static <T> Stream<T> streamOf(Collection<T> collection) {
        return isEmpty(collection) ? Stream.empty() : collection.stream();
    }

    public static <T> Collection<T> concat(Collection<? extends T> collection1, Collection<? extends T> collection2) {
        return Stream.concat(streamOf(collection1), streamOf(collection2)).collect(Collectors.toList());
    }
}
