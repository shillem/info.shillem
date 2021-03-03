package info.shillem.util.xsp.bean;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import info.shillem.util.xsp.helper.component.Controller;
import info.shillem.util.xsp.helper.component.SelectItems;
import info.shillem.util.xsp.model.ReadableDataObject;

public abstract class PageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private ReadableDataObject<Controller> controllers;
    private ReadableDataObject<SelectItems> selectItems;

    protected PageBean() {
        controllers = new ReadableDataObject<>(Controller.class, this::createTypedObject);
        selectItems = new ReadableDataObject<>(SelectItems.class, this::createTypedObject);
    }

    public void afterRenderResponse(PhaseEvent event) {

    }

    public void beforeRenderResponse(PhaseEvent event) {

    }

    protected <T> T createTypedObject(Class<T> cls, String id) {
        throw new UnsupportedOperationException(cls.getName().concat(":").concat(id));
    }

    public ReadableDataObject<Controller> getControllers() {
        return controllers;
    }

    public ReadableDataObject<SelectItems> getSelectItems() {
        return selectItems;
    }

    public void init(FacesContext facesContext) {
        
    }

}
