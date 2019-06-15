package info.shillem.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public enum OptionalUtil {
    ;
    
    @SafeVarargs
    public static <T> Optional<T> firstNonNull(Supplier<T>... suppliers) {
        return Arrays.asList(suppliers)
                .stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

}
