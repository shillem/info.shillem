package info.shillem.domino.xsp.factory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;

import info.shillem.domino.factory.AbstractDominoFactory;
import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public class XspDominoFactory extends AbstractDominoFactory {

    public static class Builder {

        private final String username;
        private final Set<Option> options;

        private DatabasePath databasePath;

        public Builder(String username) {
            this.username = Objects.requireNonNull(username, "Username cannot be null");
            this.options = new HashSet<>();
        }

        public Builder addOption(Option option) {
            options.add(option);

            return this;
        }

        public XspDominoFactory build() throws NotesException {
            try {
                long userHandle = NotesUtil.createUserNameList(username);
                Session session = XSPNative.createXPageSession(username, userHandle, true, false);

                if (databasePath != null) {
                    Database database = session.getDatabase(
                            databasePath.getServerName(),
                            databasePath.getFilePath());

                    XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
                }

                return new XspDominoFactory(
                        session,
                        options,
                        s -> Unthrow.on(() -> {
                            if (userHandle != 0) {
                                Os.OSUnlock(userHandle);
                                Os.OSMemFree(userHandle);
                            }

                            DominoUtil.recycle(s);
                        }));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Builder setCurrentDatabase(DatabasePath databasePath) {
            this.databasePath = databasePath;

            return this;
        }

    }

    public enum Option {
        DO_NOT_CONVERT_MIME, PREFER_JAVA_DATES, TRACK_MILLISEC_IN_JAVA_DATES
    }

    private final Set<Option> options;
    private Consumer<Session> recycler;

    private XspDominoFactory(Session session, Set<Option> options, Consumer<Session> recycler)
            throws NotesException {
        super(session);

        this.options = options;
        this.recycler = recycler;

        getSession().setConvertMime(
                !options.contains(Option.DO_NOT_CONVERT_MIME));
        getSession().setTrackMillisecInJavaDates(
                options.contains(Option.TRACK_MILLISEC_IN_JAVA_DATES));
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public DominoFactory newInstance(Session session) throws NotesException {
        return new XspDominoFactory(session, options, null);
    }

    @Override
    public void recycle() {
        super.recycle();

        if (recycler != null) {
            recycler.accept(getSession());
        }
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
