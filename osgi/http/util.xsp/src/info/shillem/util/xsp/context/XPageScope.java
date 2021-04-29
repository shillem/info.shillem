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
        public Map<String, Object> getValues(FacesContext context) {
            return CastUtil.toAnyMap(context.getExternalContext().getApplicationMap());
        }
    },
    FLASH {
        @Override
        public Map<String, Object> getValues(FacesContext context) {
            Map<String, Object> sessionMap = SESSION.getValues(context);

            return CastUtil.toAnyMap((Map<?, ?>) sessionMap
                    .computeIfAbsent(FLASH_SCOPE_NAME, (k) -> new HashMap<String, Object>()));
        }
    },
    PARAMETER {
        @Override
        public Map<String, Object> getValues(FacesContext context) {
            return CastUtil.toAnyMap(context.getExternalContext().getRequestParameterMap());
        }
    },
    REQUEST {
        @Override
        public Map<String, Object> getValues(FacesContext context) {
            return CastUtil.toAnyMap(context.getExternalContext().getRequestMap());
        }
    },
    SESSION {
        @Override
        public Map<String, Object> getValues(FacesContext context) {
            return CastUtil.toAnyMap(context.getExternalContext().getSessionMap());
        }
    },
    VIEW {
        @Override
        public Map<String, Object> getValues(FacesContext context) {
            UIViewRoot root = context.getViewRoot();

            if (root instanceof UIViewRootEx) {
                return CastUtil.toAnyMap(((UIViewRootEx) root).getViewMap());
            }

            return Collections.emptyMap();
        };
    };
    
    public final static String FLASH_SCOPE_NAME = "flashScope";

    public Object getValue(FacesContext context, String key) {
        return getValues(context).get(key);
    }

    public abstract Map<String, Object> getValues(FacesContext context);

    public void setValue(FacesContext context, String key, Object value) {
        getValues(context).put(key, value);
    }

}
