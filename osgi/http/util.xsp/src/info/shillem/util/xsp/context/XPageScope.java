package info.shillem.util.xsp.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.ibm.xsp.component.UIViewRootEx;

import info.shillem.util.CastUtil;

public enum XPageScope {
    APPLICATION {

        @Override
        public Map<String, Object> getValues(FacesContext facesContext, boolean computeIfAbsent) {
            return CastUtil.toAnyMap(facesContext.getExternalContext().getApplicationMap());
        }
    },
    FLASH {

        @Override
        public Map<String, Object> getValues(FacesContext facesContext, boolean computeIfAbsent) {
            Map<String, Object> sessionMap = SESSION.getValues(facesContext, computeIfAbsent);

            String key = "flashScope";

            if (computeIfAbsent) {
                return CastUtil.toAnyMap((Map<?, ?>) sessionMap
                        .computeIfAbsent(key, (k) -> new HashMap<String, Object>()));
            }

            return CastUtil.toAnyMap(
                    (Map<?, ?>) sessionMap.getOrDefault(key, Collections.emptyMap()));
        }
    },
    REQUEST {

        @Override
        public Map<String, Object> getValues(FacesContext facesContext, boolean computeIfAbsent) {
            return CastUtil.toAnyMap(facesContext.getExternalContext().getRequestMap());
        }
    },
    SESSION {

        @Override
        public Map<String, Object> getValues(FacesContext facesContext, boolean computeIfAbsent) {
            return CastUtil.toAnyMap(facesContext.getExternalContext().getSessionMap());
        }
    },
    VIEW {

        @Override
        public Map<String, Object> getValues(FacesContext facesContext, boolean computeIfAbsent) {
            UIViewRoot root = facesContext.getViewRoot();

            if (root instanceof UIViewRootEx) {
                return CastUtil.toAnyMap(((UIViewRootEx) root).getViewMap());
            }

            return Collections.emptyMap();
        };
    };

    public Object getValue(FacesContext facesContext, String key) {
        return getValues(facesContext).get(key);
    }

    public Map<String, Object> getValues(FacesContext facesContext) {
        return getValues(facesContext, true);
    }

    public abstract Map<String, Object> getValues(
            FacesContext facesContext, boolean computeIfAbsent);

    public void setValue(FacesContext facesContext, String key, Object value) {
        getValues(facesContext).put(key, value);
    }

}
