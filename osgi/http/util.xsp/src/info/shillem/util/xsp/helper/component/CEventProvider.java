package info.shillem.util.xsp.helper.component;

import java.util.ArrayList;
import java.util.List;

import info.shillem.util.xsp.dispatcher.Event;
import info.shillem.util.xsp.dispatcher.Event.Id;
import info.shillem.util.xsp.dispatcher.EventConsumer;
import info.shillem.util.xsp.dispatcher.EventProvider;

public abstract class CEventProvider implements Controller, EventProvider {

    private static final long serialVersionUID = 1L;

    private final List<EventConsumer> consumers = new ArrayList<>();

    @Override
    public void addEventConsumer(EventConsumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public final void fireEvent(Enum<? extends Event.Id> id) {
        fireEvent(new Event(id));
    }

    @Override
    public final <T> T fireEvent(Enum<? extends Id> id, Class<T> cls) {
        return fireEvent(new Event(id), cls);
    }

    @Override
    public final void fireEvent(Event event) {
        consumers.forEach(c -> c.processEvent(event));
    }

    @Override
    public final <T> T fireEvent(Event event, Class<T> cls) {
        return consumers.stream()
                .map(c -> c.processEvent(event, cls))
                .findFirst()
                .orElse(null);
    }

}
