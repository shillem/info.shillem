package info.shillem.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiFunction;

public class FnResourceBundleControl extends ResourceBundle.Control {

    public static class Builder {

        private final BiFunction<String, Locale, ResourceBundle> fn;
        private Long ttl;
        private boolean langOnly;

        public Builder(BiFunction<String, Locale, ResourceBundle> fn) {
            this.fn = Objects.requireNonNull(fn);
        }

        public FnResourceBundleControl build() {
            return new FnResourceBundleControl(this);
        }

        public Builder setLanguageOnly(boolean flag) {
            langOnly = flag;
            return this;
        }

        public Builder setTimeToLive(Long value) {
            ttl = value;
            return this;
        }

    }

    private final BiFunction<String, Locale, ResourceBundle> fn;
    private final Long ttl;
    private final boolean langOnly;

    private FnResourceBundleControl(Builder builder) {
        fn = builder.fn;
        langOnly = builder.langOnly;
        ttl = builder.ttl;
    }

    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
        if (langOnly) {
            return Arrays.asList(Locale.forLanguageTag(locale.getLanguage()), Locale.ROOT);
        }

        return super.getCandidateLocales(baseName, locale);
    }

    @Override
    public List<String> getFormats(String baseName) {
        Objects.requireNonNull(baseName);

        return Arrays.asList("fn");
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return ttl != null ? ttl.longValue() : super.getTimeToLive(baseName, locale);
    }
    
    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        return null;
    }

    @Override
    public boolean needsReload(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            ResourceBundle bundle,
            long loadTime) {
        Objects.requireNonNull(bundle, "Bundle cannot be null");

        return ttl != null;
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
            throw new IllegalArgumentException(format);
        }

        try {
            return fn.apply(baseName, locale);
        } catch (RuntimeException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

}
