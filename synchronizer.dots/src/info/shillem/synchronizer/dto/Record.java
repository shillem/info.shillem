package info.shillem.synchronizer.dto;

import java.util.HashMap;
import java.util.Set;

public class Record {

    private final HashMap<String, Object> values;

    private boolean _deleted;
    private boolean _new;
    
    public Record() {
        values = new HashMap<>();
    }

    public Set<String> getKeys() {
        return values.keySet();
    }

    public String getString(String key) {
        return (String) getValue(key);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public boolean isDeleted() {
        return _deleted;
    }

    public final boolean isNew() {
        return _new;
    }

    public void setDeleted(boolean flag) {
        _deleted = flag;
    }

    public final void setNew(boolean flag) {
        this._new = flag;
    }

    public void setValue(String key, Object value) {
        values.put(key, value);
    }

}
