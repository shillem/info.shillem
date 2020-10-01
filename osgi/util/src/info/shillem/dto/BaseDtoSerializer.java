package info.shillem.dto;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import info.shillem.dto.BaseDto.SchemaFilter;

public class BaseDtoSerializer extends StdSerializer<BaseDto<?>> {

    private static final long serialVersionUID = 1L;

    public BaseDtoSerializer() {
        super(BaseDto.class, true);
    }

    @Override
    public void serialize(BaseDto<?> value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();

        writeStringField("id", value.getId(), gen, provider);
        writeStringField("databaseUrl", value.getDatabaseUrl(), gen, provider);
        writeObjectField("lastModified", value.getLastModified(), gen, provider);

        gen.writeFieldName("values");
        {
            gen.writeStartObject();

            for (Enum<? extends BaseField> field : value.getSchema(SchemaFilter.SET)) {
                writeObjectField(field.name(), serializeField(field, value), gen, provider);
            }

            gen.writeEndObject();
        }

        gen.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E> & BaseField> Object serializeField(Enum<E> field, BaseDto<?> dto) {
        return ((BaseDto<E>) dto).getValue((E) field);
    }

    private void writeObjectField(
            String fieldName,
            Object value,
            JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        if (value == null && provider.getConfig().getDefaultPropertyInclusion()
                .getContentInclusion() == Include.NON_NULL) {
            return;
        }

        gen.writeObjectField(fieldName, value);
    }

    private void writeStringField(
            String fieldName,
            String value,
            JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        if (value == null && provider.getConfig().getDefaultPropertyInclusion()
                .getContentInclusion() == Include.NON_NULL) {
            return;
        }

        gen.writeStringField(fieldName, value);
    }

}
