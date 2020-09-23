package info.shillem.util.xsp;

import com.ibm.xsp.library.AbstractXspLibrary;

public class XspLibrary extends AbstractXspLibrary {

    @Override
    public String[] getDependencies() {
        return new String[] {
                "com.ibm.xsp.core.library",
                "com.ibm.xsp.extsn.library",
                "com.ibm.xsp.domino.library",
                "com.ibm.xsp.designer.library" };
    }

    @Override
    public String[] getFacesConfigFiles() {
        return new String[] { "META-INF/default-faces-config.xml" };
    }

    @Override
    public String getLibraryId() {
        return getPluginId() + ".library";
    }

    @Override
    public String getPluginId() {
        return XspLibrary.class.getPackage().getName();
    }

    @Override
    public String[] getXspConfigFiles() {
        return new String[] { "META-INF/default.xsp-config" };
    }

}
