package info.shillem.dao;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class QuerySerializer extends StdSerializer<Query<?>> {

    private static final long serialVersionUID = 1L;

    public QuerySerializer() {
        super(Query.class, true);
    }

    @Override
    public void serialize(Query<?> value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeObject(value.toMap());
    }

}
