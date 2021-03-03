package info.shillem.util.xsp.bean;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import info.shillem.util.xsp.helper.component.Controller;
import info.shillem.util.xsp.helper.component.SelectItems;
import info.shillem.util.xsp.model.TypedDataObject;

public abstract class PageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private TypedDataObject<Controller> controllers;
    private TypedDataObject<SelectItems> selectItems;

    protected PageBean() {
        controllers = new TypedDataObject<>(Controller.class, this::createTypedObject);
        selectItems = new TypedDataObject<>(SelectItems.class, this::createTypedObject);
    }

    public void afterRenderResponse(PhaseEvent event) {

    }

    public void beforeRenderResponse(PhaseEvent event) {

    }

    protected <T> T createTypedObject(Class<T> cls, String id) {
        throw new UnsupportedOperationException(cls.getName().concat(":").concat(id));
    }

    public TypedDataObject<Controller> getControllers() {
        return controllers;
    }

    public TypedDataObject<SelectItems> getSelectItems() {
        return selectItems;
    }

    public void init(FacesContext facesContext) {
        
    }

}
