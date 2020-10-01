package info.shillem.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public enum StreamUtil {
    ;

    public static <T> Stream<T> limitStream(Stream<T> stream, int limit) {
        if (limit > 0) {
            return stream.limit(limit);
        }

        return stream;
    }

    public static <T> Stream<T> parallelStream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), true);
    }

    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static <T> Stream<T> stream(Iterator<T> in) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(in, Spliterator.ORDERED), false);
    }

}
