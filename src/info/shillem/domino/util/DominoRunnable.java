package info.shillem.domino.util;

import java.util.Objects;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public abstract class DominoRunnable implements Runnable {

    private final String username;
    private final String password;

    public DominoRunnable() {
        this("", "");
    }

    public DominoRunnable(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
    }

    @Override
    public final void run() {
        Session session = null;

        try {
            NotesThread.sinitThread();

            session = NotesFactory.createSession((String) null, username, password);

            runNotes(session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DominoUtil.recycle(session);

            NotesThread.stermThread();
        }
    }

    public abstract void runNotes(Session session) throws NotesException;

}
