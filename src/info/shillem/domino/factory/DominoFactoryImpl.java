package info.shillem.domino.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import info.shillem.domino.util.DominoFactory;
import info.shillem.domino.util.DominoSilo;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class DominoFactoryImpl implements DominoFactory {

    private final Session session;
    private final Consumer<Session> sessionOnRecycle;
    private final Map<String, DominoSilo> silos;

    public DominoFactoryImpl(Session session) throws NotesException {
        this(session, null);
    }
    
    public DominoFactoryImpl(
            Session session, Consumer<Session> sessionOnRecycle) throws NotesException {
        Objects.requireNonNull(session, "Session cannot be null");
        
        session.setConvertMime(false);
        session.setTrackMillisecInJavaDates(true);
        
        this.session = session;
        this.sessionOnRecycle = sessionOnRecycle;
        this.silos = new HashMap<>();
    }

    @Override
    public void addDominoSilo(DominoSilo silo) {
        if (!containsDominoSilo(silo.getName())) {
            silo.setSession(session);
            silos.put(silo.getName(), silo);
        }
    }
    
    @Override
    public boolean containsDominoSilo(String name) {
        return silos.containsKey(name);
    }

    @Override
    public DominoSilo getDominoSilo(String name) {
        DominoSilo silo = silos.get(name); 
        
        if (silo == null) {
            throw new IllegalArgumentException("No configured silo with name " + name);
        }
        
        return silo;
    }

    @Override
    public Session getSession() throws NotesException {
        return session;
    }

    @Override
    public void recycle() {
        silos.values().forEach(DominoSilo::recycle);
        
        if (sessionOnRecycle != null) {
            sessionOnRecycle.accept(session);
        }
    }

}
