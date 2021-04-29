package info.shillem.util.xsp.listener;

import java.util.function.Consumer;

import javax.faces.context.FacesContext;

import com.ibm.xsp.event.FacesContextListener;

public class FunctionalFacesContextListener implements FacesContextListener {

    public static class Builder {
        private Consumer<FacesContext> beforeContext;
        private Consumer<FacesContext> beforeRendering;

        public FacesContextListener build() {
            return new FunctionalFacesContextListener(this);
        }

        public Builder setBeforeContext(Consumer<FacesContext> consumer) {
            beforeContext = consumer;

            return this;
        }

        public Builder setBeforeRendering(Consumer<FacesContext> consumer) {
            beforeRendering = consumer;

            return this;
        }
    }

    private final Consumer<FacesContext> beforeContext;
    private final Consumer<FacesContext> beforeRendering;

    private FunctionalFacesContextListener(Builder builder) {
        this.beforeContext = builder.beforeContext;
        this.beforeRendering = builder.beforeRendering;
    }

    @Override
    public void beforeContextReleased(FacesContext context) {
        if (beforeContext == null) {
            return;
        }

        beforeContext.accept(context);
    }

    @Override
    public void beforeRenderingPhase(FacesContext context) {
        if (beforeRendering == null) {
            return;
        }

        beforeRendering.accept(context);
    }

}
