package info.shillem.util.xsp;

import com.ibm.xsp.library.AbstractXspLibrary;

public class XspLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = XspLibrary.class.getName();
	public static final String PLUGIN_ID = XspLibrary.class.getPackage().getName();

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

}
