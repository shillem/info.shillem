package info.shillem.sql.xsp;

import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import javax.sql.DataSource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import info.shillem.sql.DataSourceService;
import info.shillem.util.Unthrow;

public class SqlActivator implements BundleActivator {

    private static DataSourceService SERVICE;

    private BundleContext context;
    private ServiceTracker<String, Driver> drivers;

    Driver loadDriver(String className) {
        Function<String, Optional<Driver>> supplier = (n) -> {
            return Optional.ofNullable(drivers.getServiceReferences())
                    .map(Arrays::asList)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter((ref) -> n.equals(ref.getProperty("className")))
                    .findFirst()
                    .map((ref) -> (Driver) drivers.getService(ref));
        };

        return supplier.apply(className)
                .orElseGet(() -> {
                    try {
                        String bundlePrefix = className.substring(0, className.lastIndexOf("."));

                        Arrays
                                .stream(context.getBundles())
                                .filter((b) -> b.getSymbolicName().startsWith(bundlePrefix)
                                        && b.getState() != Bundle.ACTIVE)
                                .forEach((b) -> Unthrow.on(() -> b.start()));

                        return supplier.apply(className).orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        drivers = new ServiceTracker<>(context, Driver.class.getName(), null);
        drivers.open();

        SERVICE = new DataSourceService(this::loadDriver);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        SERVICE = null;

        drivers.close();

        this.context = null;
    }

    public static DataSource getDataSource(Properties properties) {
        Objects.requireNonNull(properties, "Data source properties cannot be null");
        
        return SERVICE.getDataSource(properties);
    }

}
