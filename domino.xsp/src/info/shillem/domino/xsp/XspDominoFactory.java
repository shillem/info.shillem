package info.shillem.domino.xsp;

import java.util.Objects;
import java.util.function.Consumer;

import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;

import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.factory.LocalDominoFactory;
import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class XspDominoFactory extends LocalDominoFactory {

    private Consumer<Session> recycler;

    private XspDominoFactory(
            LocalDominoFactory.Builder builder, Consumer<Session> recycler) throws NotesException {
        super(builder);

        this.recycler = recycler;
    }

    @Override
    public void recycle() {
        super.recycle();

        recycler.accept(getSession());
    }

    public static DominoFactory newInstance(String username) {
        return newInstance(username, null);
    }

    public static DominoFactory newInstance(String username, DatabasePath databasePath) {
        Objects.requireNonNull(username, "Username cannot be null");

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
                    new LocalDominoFactory.Builder(session),
                    sess -> Unthrow.on(() -> {
                        if (userHandle != 0) {
                            Os.OSUnlock(userHandle);
                            Os.OSMemFree(userHandle);
                        }

                        DominoUtil.recycle(sess);
                    }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
