package info.shillem.sql;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataSourceService {

    private final SqlActivator activator;
    private final Map<String, DataSource> sources;

    DataSourceService(SqlActivator activator) {
        this.activator = activator;
        this.sources = new HashMap<>();
    }

    public DataSource getDataSource(Properties properties) {
        if (!properties.containsKey("name")) {
            throw new IllegalArgumentException("'name' property is mandatory");
        }

        if (!properties.containsKey("driver")) {
            throw new IllegalArgumentException("'driver' property is mandatory");
        }

        if (!properties.containsKey("url")) {
            throw new IllegalArgumentException("'url' property is mandatory");
        }

        return sources.computeIfAbsent(properties.getProperty("name"), (k) -> {
            String driverClassName = properties.getProperty("driver");
            Driver connectionDriver = activator
                    .loadDriver(driverClassName)
                    .orElseGet(() -> {
                        try {
                            activator.startBundle(
                                    driverClassName.substring(0, driverClassName.lastIndexOf(".")));

                            return activator.loadDriver(driverClassName).orElse(null);
                        } catch (Exception e) {
                            return null;
                        }
                    });

            if (connectionDriver == null) {
                throw new IllegalArgumentException(driverClassName + " driver is unavailable");
            }

            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("username", properties.getProperty("username"));
            connectionProperties.setProperty("password", properties.getProperty("password"));

            ConnectionFactory connectionFactory = new DriverConnectionFactory(
                    connectionDriver,
                    properties.getProperty("url"),
                    connectionProperties);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                    connectionFactory,
                    null);
            ObjectPool<PoolableConnection> pool = new GenericObjectPool<>(
                    poolableConnectionFactory);

            return new PoolingDataSource<PoolableConnection>(pool);
        });
    }

}
