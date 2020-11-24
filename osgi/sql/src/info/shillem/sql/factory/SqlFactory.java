package info.shillem.sql.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

public class SqlFactory {

    private final DataSource source;
    private Connection connection;

    public SqlFactory(DataSource source) {
        this.source = Objects.requireNonNull(source, "Source cannot be null");
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = source.getConnection();
        }
        
        return connection;
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
