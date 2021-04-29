package info.shillem.util.xsp.context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.function.Consumer;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public class CustomResponse {

    private final FacesContext context;
    private final HttpServletResponse response;

    public CustomResponse(FacesContext context) {
        this.context = context;
        this.response = XPageUtil.getHttpServletResponse(context);
        this.response.reset();
    }
    
    public CustomResponse setFileHeader(String fileName, String contentDisposition) {
        response.setContentType(
                URLConnection.getFileNameMap().getContentTypeFor(fileName));
        response.setHeader("Content-Disposition",
                String.format("%s;filename=\"%s\"", contentDisposition, fileName));
        
        return this;
    }

    public void send(Consumer<OutputStream> consumer) {
        try {
            consumer.accept(response.getOutputStream());
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            context.responseComplete();
        }
    }

}
