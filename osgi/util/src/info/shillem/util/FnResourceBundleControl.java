package info.shillem.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;

public class FnResourceBundleControl extends ResourceBundle.Control {
    
    private final Function<Locale, ResourceBundle> fn;

    public FnResourceBundleControl(Function<Locale, ResourceBundle> fn) {
        this.fn = Objects.requireNonNull(fn);
    }

    @Override
    public List<String> getFormats(String baseName) {
        Objects.requireNonNull(baseName);

        return Arrays.asList("fn");
    }

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        Objects.requireNonNull(baseName, "Base name cannot be null");
        Objects.requireNonNull(locale, "Locale cannot be null");
        Objects.requireNonNull(format, "Format cannot be null");
        Objects.requireNonNull(loader, "Loader cannot be null");

        if (!"fn".equals(format)) {
            return null;
        }

        try {
            return fn.apply(locale);
        } catch (RuntimeException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    @Override
    public String toBundleName(String bundleName, Locale locale) {
        Objects.requireNonNull(bundleName, "Bundle name cannot be null");
        Objects.requireNonNull(locale, "Locale cannot be null");

        return bundleName + locale.getLanguage();
    }

}
