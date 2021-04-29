package info.shillem.util.xsp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.ibm.xsp.extlib.minifier.ExtLibLoaderExtension;

public class XspActivator implements BundleActivator {

    private XspLoader loader;

    @Override
    public void start(BundleContext context) throws Exception {
        loader = new XspLoader(context);

        ExtLibLoaderExtension.getExtensions().add(loader);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (loader != null) {
            ExtLibLoaderExtension.getExtensions().remove(loader);

            loader = null;
        }
    }

}
