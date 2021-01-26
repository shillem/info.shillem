package info.shillem.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtil {
    
    private OptionalUtil() {
        throw new UnsupportedOperationException();
    }
    
    @SafeVarargs
    public static <T> Optional<T> firstNonNull(Supplier<T>... suppliers) {
        return Arrays.asList(suppliers)
                .stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

}
