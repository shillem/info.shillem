package info.shillem.util.xsp.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import info.shillem.util.xsp.context.XPageUtil;
import info.shillem.util.xsp.context.XPageScope;

public class FlashScopePhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent phaseEvent) {
        FacesContext facesContext = phaseEvent.getFacesContext();

        if (!"POST".equals(XPageUtil.getHttpServletRequest(facesContext).getMethod())) {
            XPageScope.FLASH.getValues(facesContext).clear();
        }
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent) {
        // Do nothing
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

}
