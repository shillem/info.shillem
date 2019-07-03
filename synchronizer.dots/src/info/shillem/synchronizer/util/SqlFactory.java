package info.shillem.synchronizer.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class SqlFactory {

    private final String connectionUrl;
    private final Driver driver;

    private Connection conn;

    public SqlFactory(Driver driver, String connectionUrl) {
        this.connectionUrl = Objects.requireNonNull(connectionUrl, "Connection URL cannot be null");
        this.driver = Objects.requireNonNull(driver, "Driver cannot be null");
    }

    public Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = driver.connect(connectionUrl, new Properties());
            conn.setAutoCommit(false);
        }

        return conn;
    }

    public void recycle() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Do nothing
            }
        }
    }

}
