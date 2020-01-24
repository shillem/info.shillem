package info.shillem.domino.util;

public interface DbIdentifier {

    default String getName() {
        return getClass().getCanonicalName() + "." + toString();
    }

}
