package info.shillem.util;

import java.io.Serializable;
import java.util.function.BiFunction;

public interface SBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {

}
