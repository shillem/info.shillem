package info.shillem.domino.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import info.shillem.domino.util.DominoSilo;
import lotus.domino.Session;

abstract class AbstractDominoFactory implements DominoFactory {

    private final Session session;
    private final Consumer<Session> sessionOnRecycle;
    private final Map<String, DominoSilo> silos;

    AbstractDominoFactory(Session session) {
        this(session, null);
    }

    AbstractDominoFactory(
            Session session, Consumer<Session> sessionOnRecycle) {
        this.session = session;
        this.sessionOnRecycle = sessionOnRecycle;
        this.silos = new HashMap<>();
    }

    @Override
    public void addDominoSilo(DominoSilo silo) {
        silos.computeIfAbsent(silo.getName(), (key) -> {
            silo.setSession(session);
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
