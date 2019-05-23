package info.shillem.domino.factory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public class DiiopDominoFactory extends AbstractDominoFactory {

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

        public DiiopDominoFactory build() throws NotesException {
            return new DiiopDominoFactory(this);
        }

    }

    public enum Option {
        DO_NOT_CONVERT_MIME
    }

    private final Set<Option> options;

    private DiiopDominoFactory(Builder builder) throws NotesException {
        super(builder.session, builder.sessionOnRecycle);

        options = builder.options;

        getSession().setConvertMime(
                !options.contains(Option.DO_NOT_CONVERT_MIME));
    }

    @Override
    public Document setDefaults(Document doc) throws NotesException {
        return doc;
    }

    @Override
    public ViewEntry setDefaults(ViewEntry entry) throws NotesException {
        return entry;
    }

}
