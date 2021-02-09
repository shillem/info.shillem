package info.shillem.sql.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

public class SqlFactory {

    public static class Builder {

        private final DataSource source;
        private final Set<Option> options;

        public Builder(DataSource source) {
            this.source = Objects.requireNonNull(source, "Data source cannot be null");
            this.options = new HashSet<>();
        }

        public Builder addOption(Option option) {
            options.add(option);

            return this;
        }

        public SqlFactory build() {
            return new SqlFactory(this);
        }

    }

    public enum Option {
        PREFER_INSENSITIVE_LIKE
    }

    private final Set<Option> options;
    private final DataSource source;
    private Connection connection;

    private SqlFactory(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null");

        source = builder.source;
        options = builder.options;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = source.getConnection();
        }

        return connection;
    }

    public SqlFactory newInstance(DataSource source) {
        Builder builder = new Builder(source);

        options.forEach(builder::addOption);

        return new SqlFactory(builder);
    }

    public void recycle() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
