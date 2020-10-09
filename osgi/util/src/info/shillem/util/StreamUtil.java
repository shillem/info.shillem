package info.shillem.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public enum StreamUtil {
    ;

    public static <T> Stream<T> parallelStream(Iterable<T> value) {
        return StreamSupport.stream(value.spliterator(), true);
    }

    public static <T> Stream<T> stream(Iterable<T> value) {
        return StreamSupport.stream(value.spliterator(), false);
    }

    public static <T> Stream<T> stream(Iterator<T> value) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(value, Spliterator.ORDERED), false);
    }

}
