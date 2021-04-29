package info.shillem.util.xsp;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.ibm.xsp.extlib.minifier.ExtLibLoaderExtension;
import com.ibm.xsp.extlib.resources.ExtlibResourceProvider;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class XspLoader extends ExtLibLoaderExtension {

    public static final String NAMESPACE = "shillem";

    private BundleContext context;

    public XspLoader(BundleContext context) {
        this.context = context;
    }

    @Override
    public Bundle getOSGiBundle() {
        return context.getBundle();
    }

    @Override
    public URL getResourceURL(HttpServletRequest request, String name) {
        if (name.startsWith(NAMESPACE)) {
            String path = ExtlibResourceProvider.BUNDLE_RES_PATH.concat(name);

            return ExtLibUtil.getResourceURL(getOSGiBundle(), path);
        }

        return null;
    }

}
