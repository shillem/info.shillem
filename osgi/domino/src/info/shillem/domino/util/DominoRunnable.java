package info.shillem.domino.util;

import java.util.Objects;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public abstract class DominoRunnable implements Runnable {

    private String username;
    private String password;

    public DominoRunnable() {

    }

    public DominoRunnable(String username, String password) {
        setCredentials(username, password);
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public final void run() {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

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
