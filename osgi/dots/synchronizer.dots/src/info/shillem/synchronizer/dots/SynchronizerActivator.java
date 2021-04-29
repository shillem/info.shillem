package info.shillem.synchronizer.dots;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import info.shillem.synchronizer.util.ProcessorBuilder;

public class SynchronizerActivator implements BundleActivator {

    public static final String PLUGIN_ID = SynchronizerActivator.class.getPackage().getName();

    private static ServiceTracker BUILDERS;

    public void start(BundleContext context) throws Exception {
        BUILDERS = new ServiceTracker(context, ProcessorBuilder.class.getName(), null);
        BUILDERS.open();
    }

    public void stop(BundleContext context) throws Exception {
        BUILDERS.close();
        BUILDERS = null;
    }

    public static ProcessorBuilder getProcessorBuilder(
            String className) throws ClassNotFoundException {
        Objects.requireNonNull(className, "Processor builder class name cannot be null");

        return Optional.ofNullable(BUILDERS.getServiceReferences())
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .filter((ref) -> className.equals(ref.getProperty("className")))
                .findFirst()
                .map((ref) -> (ProcessorBuilder) BUILDERS.getService(ref))
                .orElseThrow(() -> new ClassNotFoundException(className));
    }

}
