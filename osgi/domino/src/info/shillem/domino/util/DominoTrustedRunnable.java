package info.shillem.domino.util;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public abstract class DominoTrustedRunnable implements Runnable {

    @Override
    public final void run() {
        Session session = null;

        try {
            NotesThread.sinitThread();

            session = NotesFactory.createTrustedSession();

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
