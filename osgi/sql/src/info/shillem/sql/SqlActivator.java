package info.shillem.sql;

import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import info.shillem.util.Unthrow;

public class SqlActivator implements BundleActivator {

    private static DataSourceService SERVICE;

    private BundleContext context;
    private ServiceTracker<Driver, Driver> drivers;

    Optional<Driver> loadDriver(String className) {
        return Optional
                .ofNullable(drivers.getServiceReferences())
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .filter((sr) -> className.equals(sr.getProperty("className")))
                .findFirst()
                .map((ref) -> (Driver) drivers.getService(ref));
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        drivers = new ServiceTracker<>(context, Driver.class, null);
        drivers.open();

        SERVICE = new DataSourceService(this);
    }

    void startBundle(String name) {
        Arrays
                .stream(context.getBundles())
                .filter((b) -> b.getSymbolicName().startsWith(name)
                        && b.getState() != Bundle.ACTIVE)
                .forEach((b) -> Unthrow.on(() -> b.start()));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        SERVICE = null;

        drivers.close();

        this.context = null;
    }

    public static DataSource getDataSource(Properties properties) {
        return SERVICE.getDataSource(properties);
    }

}
