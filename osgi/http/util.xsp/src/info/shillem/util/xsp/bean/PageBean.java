package info.shillem.util.xsp.bean;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

public interface PageBean {

    void afterRenderResponse(PhaseEvent event);

    public void beforeRenderResponse(PhaseEvent event);

    void init(FacesContext facesContext);

}
