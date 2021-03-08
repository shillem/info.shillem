package info.shillem.sql;

import java.sql.Driver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataSourceService {

    private static final List<String> INTERNAL_MANDATORY_KEYS =
            Arrays.asList("name", "driverClassName", "url");

    private final Function<String, Driver> loader;
    private final Map<String, DataSource> sources;

    public DataSourceService(Function<String, Driver> loader) {
        this.loader = Objects.requireNonNull(loader, "Driver loader cannot be null");
        this.sources = new HashMap<>();
    }

    private DriverConnectionFactory createDriverConnectionFactory(Properties properties) {
        String driverClassName = properties.getProperty("driverClassName");
        Driver connectionDriver = loader.apply(driverClassName);

        if (connectionDriver == null) {
            throw new IllegalArgumentException(driverClassName + " driver is unavailable");
        }

        Properties connectionProperties = new Properties();

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String prop = (String) entry.getKey();

            if (!prop.startsWith("connection.")) {
                continue;
            }

            connectionProperties.setProperty(
                    prop.replace("connection.", ""), (String) entry.getValue());
        }

        return new DriverConnectionFactory(
                connectionDriver,
                properties.getProperty("url"),
                connectionProperties);
    }

    private GenericObjectPool<PoolableConnection> createGenericObjectPool(
            PoolableConnectionFactory connectionFactory,
            Properties properties) {
        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(connectionFactory);

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String prop = (String) entry.getKey();

            switch (prop) {
            case "pool.maxTotal":
                pool.setMaxTotal(Integer.valueOf((String) entry.getValue()));
                break;
            case "pool.maxWaitMillis":
                pool.setMaxWaitMillis(Long.valueOf((String) entry.getValue()));
                break;
            case "pool.testOnBorrow":
                pool.setTestOnBorrow(Boolean.valueOf((String) entry.getValue()));
                break;
            }
        }

        return pool;
    }

    private PoolableConnectionFactory createPoolableConnectionFactory(
            ConnectionFactory connectionFactory,
            Properties properties) {
        PoolableConnectionFactory poolable = new PoolableConnectionFactory(connectionFactory, null);

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String prop = (String) entry.getKey();

            switch (prop) {
            case "pool.defaultQueryTimeout":
                poolable.setDefaultQueryTimeout(Integer.valueOf((String) entry.getValue()));
                break;
            case "pool.validationQuery":
                poolable.setValidationQuery((String) entry.getValue());
                break;
            case "pool.validationQueryTimeout":
                poolable.setValidationQueryTimeout(Integer.valueOf((String) entry.getValue()));
                break;
            }
        }

        return poolable;
    }

    public DataSource getDataSource(Properties properties) {
        INTERNAL_MANDATORY_KEYS.forEach((k) -> {
            if (!properties.containsKey(k)) {
                throw new IllegalArgumentException(String.format("'%s' property is mandatory", k));
            }
        });

        return sources.computeIfAbsent(properties.getProperty("name"), (k) -> {
            DriverConnectionFactory connection =
                    createDriverConnectionFactory(properties);
            PoolableConnectionFactory poolable =
                    createPoolableConnectionFactory(connection, properties);
            GenericObjectPool<PoolableConnection> pool =
                    createGenericObjectPool(poolable, properties);

            poolable.setPool(pool);
            
            return new PoolingDataSource<PoolableConnection>(pool);
        });
    }

}
