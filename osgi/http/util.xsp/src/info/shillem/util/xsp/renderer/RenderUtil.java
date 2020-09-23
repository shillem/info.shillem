package info.shillem.util.xsp.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.component.FacesAttrsObject;
import com.ibm.xsp.renderkit.html_basic.AttrsUtil;

public enum RenderUtil {
    ;

    public static void endElement(ResponseWriter writer, String name) throws IOException {
        writer.endElement(name);
    }

    public static void startElement(ResponseWriter writer, String name) throws IOException {
        startElement(writer, name, null);
    }

    public static void startElement(
            ResponseWriter writer, String name, UIComponent component) throws IOException {
        writer.startElement(name, component);
    }

    public static void writeAttribute(
            ResponseWriter writer, String name, Object value) throws IOException {
        writeAttribute(writer, name, value, null);
    }

    public static void writeAttribute(
            ResponseWriter writer, String name, Object value, String property) throws IOException {
        if (value != null) {
            writer.writeAttribute(name, value, property);
        }
    }

    public static void writeAttribute(
            ResponseWriter writer, String name, UIComponent component) throws IOException {
        writeAttribute(writer, name, component.getAttributes().get(name), name);
    }

    public static void writeEscapedText(ResponseWriter writer, Object value) throws IOException {
        if (value != null) {
            writer.writeText(value, null);
        }
    }

    public static void writeFacesAttrs(
            FacesContext facesContext, ResponseWriter writer, UIComponent component)
            throws IOException {
        if (component instanceof FacesAttrsObject) {
            AttrsUtil.encodeAttrs(facesContext, writer, (FacesAttrsObject) component);
        }
    }

    public static void writeNewLine(ResponseWriter writer) throws IOException {
        writeUnescapedText(writer, "\n");
    }

    public static void writeUnescapedText(ResponseWriter writer, Object value) throws IOException {
        if (value != null) {
            writer.write(value.toString());
        }
    }

    public static void writeURIAttribute(
            ResponseWriter writer, String name, Object value) throws IOException {
        writeURIAttribute(writer, name, value, null);
    }

    public static void writeURIAttribute(
            ResponseWriter writer, String name, Object value, String property) throws IOException {
        if (value != null) {
            writer.writeURIAttribute(name, value, property);
        }
    }

}
