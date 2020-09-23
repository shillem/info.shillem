package info.shillem.domino.util;

import java.util.Objects;

public class StringDbIdentifier implements DbIdentifier {

    private final String name;

    public StringDbIdentifier(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof StringDbIdentifier) {
            StringDbIdentifier id = (StringDbIdentifier) obj;

            return Objects.equals(getName(), id.getName());
        }

        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + name.hashCode();

        return result;
    }

}
