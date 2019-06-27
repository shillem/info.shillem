package info.shillem.synchronizer.dots;

import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import info.shillem.synchronizer.util.ProcessorBuilder;

public class SynchronizerActivator implements BundleActivator {

    public static final String PLUGIN_ID = SynchronizerActivator.class.getPackage().getName();

    private static ServiceTracker sqlDriverServiceTracker;
    private static ServiceTracker processorServiceTracker;

    public void start(BundleContext bundleContext) throws Exception {
        sqlDriverServiceTracker = new ServiceTracker(
                bundleContext, java.sql.Driver.class.getName(), null);
        sqlDriverServiceTracker.open();

        processorServiceTracker = new ServiceTracker(
                bundleContext, ProcessorBuilder.class.getName(), null);
        processorServiceTracker.open();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        processorServiceTracker.close();

        sqlDriverServiceTracker.close();
    }

    static ProcessorBuilder getProcessorBuilder(String className)
            throws ClassNotFoundException {
        return getProcessorServiceReferences()
                .filter((ref) -> className.equals(ref.getProperty("className")))
                .findFirst()
                .map((ref) -> (ProcessorBuilder) processorServiceTracker.getService(ref))
                .orElseThrow(() -> new ClassNotFoundException(className));
    }

    static List<String> getProcessorClassNames() {
        return getProcessorServiceReferences()
                .map((ref) -> (String) ref.getProperty("className"))
                .sorted()
                .collect(Collectors.toList());
    }

    private static Stream<ServiceReference> getProcessorServiceReferences() {
        return Optional
                .ofNullable(processorServiceTracker.getServiceReferences())
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream();
    }

    static Driver getSqlDriver(String className) throws ClassNotFoundException {
        return getSqlDriverServiceReferences()
                .filter((ref) -> className.equals(ref.getProperty("className")))
                .findFirst()
                .map((ref) -> (Driver) sqlDriverServiceTracker.getService(ref))
                .orElseThrow(() -> new ClassNotFoundException(className));
    }

    static List<String> getSqlDriverClassNames() {
        return getSqlDriverServiceReferences()
                .map((ref) -> (String) ref.getProperty("className"))
                .sorted()
                .collect(Collectors.toList());
    }

    private static Stream<ServiceReference> getSqlDriverServiceReferences() {
        return Optional
                .ofNullable(sqlDriverServiceTracker.getServiceReferences())
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream();
    }

}
