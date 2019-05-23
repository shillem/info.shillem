package info.shillem.domino.factory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public class LocalDominoFactory extends AbstractDominoFactory {

    public static class Builder {

        private final Session session;
        private final Consumer<Session> sessionOnRecycle;
        private final Set<Option> options;

        public Builder(Session session) {
            this(session, null);
        }

        public Builder(Session session, Consumer<Session> sessionOnRecycle) {
            this.session = Objects.requireNonNull(session, "Session cannot be null");
            this.sessionOnRecycle = sessionOnRecycle;
            this.options = new HashSet<>();
        }

        public Builder addOption(Option option) {
            options.add(option);

            return this;
        }

        public LocalDominoFactory build() throws NotesException {
            return new LocalDominoFactory(this);
        }

    }

    public enum Option {
        DO_NOT_CONVERT_MIME, PREFER_JAVA_DATES, TRACK_MILLISEC_IN_JAVA_DATES
    }

    private final Set<Option> options;

    private LocalDominoFactory(Builder builder) throws NotesException {
        super(builder.session, builder.sessionOnRecycle);

        options = builder.options;

        getSession().setConvertMime(
                !options.contains(Option.DO_NOT_CONVERT_MIME));
        getSession().setTrackMillisecInJavaDates(
                options.contains(Option.TRACK_MILLISEC_IN_JAVA_DATES));
    }

    @Override
    public Document setDefaults(Document doc) throws NotesException {
        doc.setPreferJavaDates(options.contains(Option.PREFER_JAVA_DATES));

        return doc;
    }

    @Override
    public ViewEntry setDefaults(ViewEntry entry) throws NotesException {
        entry.setPreferJavaDates(options.contains(Option.PREFER_JAVA_DATES));

        return entry;
    }

}
