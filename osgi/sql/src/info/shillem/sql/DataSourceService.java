package info.shillem.sql;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataSourceService {

    private final Function<String, Driver> loader;
    private final Map<String, DataSource> sources;

    public DataSourceService(Function<String, Driver> loader) {
        this.loader = Objects.requireNonNull(loader, "Driver loader cannot be null");
        this.sources = new HashMap<>();
    }

    public DataSource getDataSource(Properties properties) {
        if (!properties.containsKey("name")) {
            throw new IllegalArgumentException("'name' property is mandatory");
        }

        if (!properties.containsKey("driverClassName")) {
            throw new IllegalArgumentException("'driverClassName' property is mandatory");
        }

        if (!properties.containsKey("url")) {
            throw new IllegalArgumentException("'url' property is mandatory");
        }

        return sources.computeIfAbsent(properties.getProperty("name"), (k) -> {
            String driverClassName = properties.getProperty("driverClassName");
            Driver connectionDriver = loader.apply(driverClassName);

            if (connectionDriver == null) {
                throw new IllegalArgumentException(driverClassName + " driver is unavailable");
            }

            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("username", properties.getProperty("username"));
            connectionProperties.setProperty("password", properties.getProperty("password"));
            connectionProperties.setProperty("connectionProperties",
                    properties.getProperty("connectionProperties"));

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
