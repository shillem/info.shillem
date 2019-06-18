package info.shillem.util.dots;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.ibm.dots.utils.Platform;

import info.shillem.util.dots.lang.PreferencesException;

public class Preferences {

    public static class Property {

        private final String name;
        private final Class<?> type;
        private final PropertyPolicy policy;
        private final Object defaultValue;

        private Object value;

        public Property(String name, Class<?> type, PropertyPolicy policy) {
            this(name, type, policy, null);
        }

        public <T> Property(String name, Class<T> type, PropertyPolicy policy, T defaultValue) {
            this.name = Objects.requireNonNull(name, "Name cannot be null");
            this.type = Objects.requireNonNull(type, "Type cannot be null");
            this.policy = Objects.requireNonNull(policy, "Policy cannot be null");

            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public final String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public <T> T getValue(Class<T> cls) {
            return cls.cast(value);
        }

        private boolean isMandatory() {
            switch (policy) {
            case IN_MEMORY_MANDATORY:
            case ON_DISK_MANDATORY:
                return true;
            default:
                return false;
            }
        }

        private boolean isStoredOnDisk() {
            switch (policy) {
            case ON_DISK:
            case ON_DISK_MANDATORY:
                return true;
            default:
                return false;
            }
        }

        public void setValue(Object value) {
            if (isMandatory()) {
                Objects.requireNonNull(value, "Value cannot be null");
            }

            if (value != null && !value.getClass().isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        String.format("Cannot assign value of type %s to property of type %s",
                                value.getClass().getName(),
                                type.getName()));
            }

            this.value = value;
        }

    }

    public enum PropertyPolicy {
        IN_MEMORY,
        IN_MEMORY_MANDATORY,
        ON_DISK,
        ON_DISK_MANDATORY
    }

    private final String pluginId;
    private final Map<String, Property> properties;
    private boolean readOnce;

    public Preferences(String pluginId, Set<Property> properties) {
        this.pluginId = Objects.requireNonNull(pluginId, "Plug-in id cannot be null");
        this.properties = Optional
                .ofNullable(properties)
                .orElse(Collections.emptySet())
                .stream()
                .collect(Collectors.toMap(Property::getName, (prop) -> prop));
    }

    public Property getProperty(String name) {
        if (!properties.containsKey(name)) {
            throw new PreferencesException("Invalid preference property " + name);
        }

        return properties.get(name);
    }

    public String getHelp() {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);

        writer.println("configuration");

        try {
            properties.values()
                    .stream()
                    .forEach((prop) -> writer.println(prop.getName() + "=" + prop.value));
        } catch (PreferencesException e) {
            writer.println(e.getMessage());
        }

        return out.toString();
    }

    public synchronized void readPropertiesOnDisk() {
        IEclipsePreferences preferences = Platform.getPreferences(pluginId);
        
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }

        properties
                .values()
                .stream()
                .filter(Property::isStoredOnDisk)
                .forEach((prop) -> {
                    Object value = valueFromPreferences(prop, preferences);

                    if (prop.isMandatory() && value == null) {
                        throw new PreferencesException(
                                String.format("Value for preference %s is required",
                                        prop.getName()));
                    }

                    prop.setValue(value);
                });

        readOnce = true;
    }

    public synchronized void savePropertiesOnDisk() {
        if (!readOnce) {
            throw new PreferencesException(
                    "Properties cannot be saved on disk"
                            + " unless readPropertiesOnDisk gets called at least once before");
        }

        IEclipsePreferences preferences = Platform.getPreferences(pluginId);

        properties
                .values()
                .stream()
                .filter(Property::isStoredOnDisk)
                .forEach((prop) -> {
                    String value = valueToPreferences(prop);

                    if (!value.equals(preferences.get(prop.getName(), ""))) {
                        preferences.put(prop.getName(), value);
                    }
                });

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private Object valueFromPreferences(Property prop, IEclipsePreferences preferences) {
        Optional<String> opt = Optional
                .ofNullable(preferences.get(prop.getName(), null))
                .filter((s) -> !s.isEmpty());

        if (String.class.isAssignableFrom(prop.type)) {
            return opt.orElse((String) prop.defaultValue);
        }

        if (Boolean.class.isAssignableFrom(prop.type)) {
            return opt.map(Boolean::valueOf).orElse((Boolean) prop.defaultValue);
        }

        if (Double.class.isAssignableFrom(prop.type)) {
            return opt.map(Double::valueOf).orElse((Double) prop.defaultValue);
        }

        if (Integer.class.isAssignableFrom(prop.type)) {
            return opt.map(Integer::valueOf).orElse((Integer) prop.defaultValue);
        }

        if (LocalDate.class.isAssignableFrom(prop.type)) {
            return opt.map(LocalDate::parse).orElse((LocalDate) prop.defaultValue);
        }

        if (LocalDateTime.class.isAssignableFrom(prop.type)) {
            return opt.map(LocalDateTime::parse).orElse((LocalDateTime) prop.defaultValue);
        }

        if (Long.class.isAssignableFrom(prop.type)) {
            return opt.map(Long::valueOf).orElse((Long) prop.defaultValue);
        }

        throw new UnsupportedOperationException(
                prop.type.getName() + " is unsupported property type");
    }

    private String valueToPreferences(Property prop) {
        if (Objects.isNull(prop.value)) {
            return "";
        }

        if (String.class.isAssignableFrom(prop.type)) {
            return (String) prop.value;
        }

        if (Boolean.class.isAssignableFrom(prop.type)) {
            return ((Boolean) prop.value).toString();
        }

        if (Double.class.isAssignableFrom(prop.type)) {
            return ((Double) prop.value).toString();
        }

        if (Integer.class.isAssignableFrom(prop.type)) {
            return ((Integer) prop.value).toString();
        }

        if (LocalDate.class.isAssignableFrom(prop.type)) {
            return DateTimeFormatter.ISO_DATE.format((LocalDate) prop.value);
        }

        if (LocalDateTime.class.isAssignableFrom(prop.type)) {
            return DateTimeFormatter.ISO_DATE_TIME.format((LocalDateTime) prop.value);
        }

        if (Long.class.isAssignableFrom(prop.type)) {
            return ((Long) prop.value).toString();
        }

        throw new UnsupportedOperationException(
                prop.type.getName() + " is unsupported property type");
    }

}
