package info.shillem.util.xsp.renderer;

import java.io.IOException;

import javax.faces.context.ResponseWriter;

import com.ibm.xsp.renderkit.html_basic.ListboxRenderer;

public class UIListboxRenderer extends ListboxRenderer {

    @Override
    protected void writeDefaultSize(ResponseWriter writer, int itemCount) throws IOException {
        // Do nothing
    }

}
