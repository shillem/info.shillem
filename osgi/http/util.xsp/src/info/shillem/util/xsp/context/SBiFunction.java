package info.shillem.util.xsp.context;

import java.io.Serializable;
import java.util.function.BiFunction;

public interface SBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {

}
