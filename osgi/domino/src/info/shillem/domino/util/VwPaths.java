package info.shillem.domino.util;

import java.util.Map;
import java.util.TreeMap;

public class VwPaths {

    private final Map<String, VwPath> values;

    public VwPaths() {
        values = new TreeMap<>();
    }

    public VwPaths add(VwPath path) {
        values.put(path.getAlias(), path);

        return this;
    }
    
    public VwPath get(String alias) {
        return values.get(alias);
    }

}