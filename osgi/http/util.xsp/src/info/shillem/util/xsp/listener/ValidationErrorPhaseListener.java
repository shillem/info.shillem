package info.shillem.util.xsp.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import info.shillem.util.xsp.context.XPageUtil;

public class ValidationErrorPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent phaseEvent) {
        FacesContext facesContext = phaseEvent.getFacesContext();

        if (facesContext.getMessages().hasNext()) {
            XPageUtil.setResponseErrorHeader(facesContext, getPhaseId());
        }
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent) {
        //
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.PROCESS_VALIDATIONS;
    }

}
