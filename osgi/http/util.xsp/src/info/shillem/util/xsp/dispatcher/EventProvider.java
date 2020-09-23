package info.shillem.util.xsp.dispatcher;

public interface EventProvider {

    void addEventConsumer(EventConsumer consumer);

    void fireEvent(Event event);

    void fireEvent(Enum<? extends Event.Id> id);
    
    <T> T fireEvent(Enum<? extends Event.Id> id, Class<T> cls);
    
    <T> T fireEvent(Event event, Class<T> cls);

}
