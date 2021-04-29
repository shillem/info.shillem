package info.shillem.util.xsp.dispatcher;

public interface EventConsumer {

    void processEvent(Event event);

    <T> T processEvent(Event event, Class<T> cls);

}
