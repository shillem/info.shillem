package info.shillem.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AttachmentFilesSerializer extends StdSerializer<AttachmentFiles> {

    private static final long serialVersionUID = 1L;

    public AttachmentFilesSerializer() {
        super(AttachmentFiles.class, true);
    }

    @Override
    public void serialize(AttachmentFiles value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartArray();
        {
            for (AttachmentFile file : ((AttachmentFiles) value).getAll()) {
                gen.writeObject(file);
            }
        }
        gen.writeEndArray();
    }

}
