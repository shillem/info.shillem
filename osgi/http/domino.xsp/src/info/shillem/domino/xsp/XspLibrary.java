package info.shillem.domino.xsp;

import com.ibm.xsp.library.AbstractXspLibrary;

public final class XspLibrary extends AbstractXspLibrary {

    @Override
    public String getLibraryId() {
        return getPluginId() + ".library";
    }

    @Override
    public String getPluginId() {
        return this.getClass().getPackage().getName();
    }

}
