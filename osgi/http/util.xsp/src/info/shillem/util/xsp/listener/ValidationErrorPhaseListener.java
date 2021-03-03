package info.shillem.util.xsp.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import info.shillem.util.xsp.context.XPageUtil;

public class ValidationErrorPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();

        if (facesContext.getMessages().hasNext()) {
            XPageUtil.setResponseStatusCode(facesContext, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.PROCESS_VALIDATIONS;
    }

}
