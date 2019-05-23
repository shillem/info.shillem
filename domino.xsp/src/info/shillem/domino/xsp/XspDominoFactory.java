package info.shillem.domino.xsp;

import java.util.Objects;

import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;

import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.factory.LocalDominoFactory;
import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.Session;

public class XspDominoFactory {

    private XspDominoFactory() {
        throw new UnsupportedOperationException();
    }

    public static DominoFactory newInstance(String userName) {
        return newInstance(userName, null);
    }

    public static DominoFactory newInstance(String userName, DatabasePath currentDatabasePath) {
        Objects.requireNonNull(userName, "Username cannot be null");

        try {
            final long userHandle =
                    NotesUtil.createUserNameList(userName);
            final Session session =
                    XSPNative.createXPageSession(userName, userHandle, true, false);

            if (currentDatabasePath != null) {
                final Database database = session.getDatabase(
                        currentDatabasePath.getServerName(), currentDatabasePath.getFilePath());

                XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
            }

            return new LocalDominoFactory.Builder(session,
                    s -> Unthrow.on(() -> {
                        if (userHandle != 0) {
                            Os.OSUnlock(userHandle);
                            Os.OSMemFree(userHandle);
                        }

                        DominoUtil.recycle(session);
                    }))
                            .addOption(LocalDominoFactory.Option.DO_NOT_CONVERT_MIME)
                            .addOption(LocalDominoFactory.Option.PREFER_JAVA_DATES)
                            .addOption(LocalDominoFactory.Option.TRACK_MILLISEC_IN_JAVA_DATES)
                            .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
