package info.shillem.domino.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.domino.util.DominoSilo;
import lotus.domino.Session;

abstract class AbstractDominoFactory implements DominoFactory {

    private static final ThreadLocal<Session> thread = new ThreadLocal<>();
    
    private final Map<String, DominoSilo> silos;
    
    AbstractDominoFactory(Session session) {
        Objects.requireNonNull(session, "Session cannot be null");
        
        if (!session.isValid()) {
            throw new IllegalArgumentException("Session must be valid");
        }
        
        thread.set(session);
        
        this.silos = new HashMap<>();
    }

    @Override
    public void addDominoSilo(DominoSilo silo) {
        silos.computeIfAbsent(silo.getName(), (key) -> {
            silo.setSession(getSession());
            return silo;
        });
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
    public Session getSession() {
        return thread.get();
    }

    @Override
    public void recycle() {
        silos.values().forEach(DominoSilo::recycle);
    }

}
