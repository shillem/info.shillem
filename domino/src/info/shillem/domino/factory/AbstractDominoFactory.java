package info.shillem.domino.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.domino.util.DbIdentifier;
import info.shillem.domino.util.DominoSilo;
import lotus.domino.Session;

public abstract class AbstractDominoFactory implements DominoFactory {
    
    private final Session session;
    private final Map<DbIdentifier, DominoSilo> silos;
    
    protected AbstractDominoFactory(Session session) {
        Objects.requireNonNull(session, "Session cannot be null");
        
        if (!session.isValid()) {
            throw new IllegalArgumentException("Session must be valid");
        }
        
        this.session = session;        
        this.silos = new HashMap<>();
    }

    @Override
    public void addSilo(DominoSilo silo) {
        silos.computeIfAbsent(silo.getIdentifier(), (key) -> {
            silo.setSession(getSession());
            return silo;
        });
    }

    @Override
    public boolean containsSilo(DbIdentifier identifier) {
        return silos.containsKey(identifier);
    }

    @Override
    public DominoSilo getSilo(DbIdentifier identifier) {
        DominoSilo silo = silos.get(identifier);

        if (silo == null) {
            throw new IllegalArgumentException(
                    "No configured silo with identifier " + identifier.getName());
        }

        return silo;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void recycle() {
        silos.values().forEach(DominoSilo::recycle);
    }

}
