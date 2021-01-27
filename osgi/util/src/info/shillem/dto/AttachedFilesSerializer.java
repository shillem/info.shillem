package info.shillem.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AttachedFilesSerializer extends StdSerializer<AttachedFiles> {

    private static final long serialVersionUID = 1L;

    public AttachedFilesSerializer() {
        super(AttachedFiles.class, true);
    }

    @Override
    public void serialize(AttachedFiles value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartArray();
        {
            for (AttachedFile file : ((AttachedFiles) value).getAll()) {
                gen.writeObject(file);
            }
        }
        gen.writeEndArray();
    }

}
