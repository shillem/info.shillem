package info.shillem.dto;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import info.shillem.dto.BaseDto.SchemaFilter;

public class BaseDtoAdapter<T extends BaseDto<E>, E extends Enum<E> & BaseField>
        extends TypeAdapter<T> {

    private final Supplier<T> supplier;

    public BaseDtoAdapter(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    protected Date parseDate(String value) {
        return Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(value)));
    }

    protected <V> V parseValue(JsonReader in, E field, Class<V> cls) throws IOException {
        if (cls == Boolean.class) {
            return cls.cast(in.nextBoolean());
        }

        if (cls == Date.class) {
            return cls.cast(parseDate(in.nextString()));
        }

        if (cls == Double.class) {
            return cls.cast(in.nextDouble());
        }

        if (cls == Integer.class) {
            return cls.cast(in.nextInt());
        }

        if (cls == String.class) {
            return cls.cast(in.nextString());
        }

        in.skipValue();

        return null;
    }

    protected <V> List<V> parseValues(JsonReader in, E field, Class<V> cls) throws IOException {
        in.beginArray();

        List<V> values = new ArrayList<V>();

        while (in.hasNext()) {
            values.add(parseValue(in, field, cls));
        }

        in.endArray();

        return values;
    }

    @Override
    public T read(JsonReader in) throws IOException {
        T dto = supplier.get();

        in.beginObject();

        while (in.hasNext()) {
            switch (in.nextName()) {
            case "id":
                dto.setId(in.nextString());
                break;
            case "databaseUrl":
                dto.setDatabaseUrl(in.nextString());
                break;
            case "lastModified":
                dto.setLastModified(parseDate(in.nextString()));
                break;
            case "values":
                in.beginObject();

                while (in.hasNext()) {
                    E field = dto.fieldOf(in.nextName());
                    FieldProperties properties = field.getProperties();

                    if (properties.isList()) {
                        dto.setValue(field, parseValues(in, field, properties.getType()));
                    } else {
                        dto.setValue(field, parseValue(in, field, properties.getType()));
                    }
                }

                in.endObject();
                break;
            }
        }

        in.endObject();

        return dto;
    }

    @Override
    public void write(JsonWriter out, T dto) throws IOException {
        out.beginObject();

        out.name("id");
        writeValue(out, dto.getId());

        out.name("databaseUrl");
        writeValue(out, dto.getDatabaseUrl());

        out.name("lastModified");
        writeValue(out, dto.getLastModified());

        out.name("values");
        {
            out.beginObject();

            for (E field : dto.getSchema(SchemaFilter.SET)) {
                writeField(out, dto, field);
            }

            out.endObject();
        }

        out.endObject();
    }

    protected void writeField(JsonWriter out, T dto, E field)
            throws IOException {
        out.name(field.name());

        if (field.getProperties().isList()) {
            out.beginArray();

            List<?> values = dto.getList(field, field.getProperties().getType());

            for (Object value : values) {
                writeValue(out, value);
            }

            out.endArray();
        } else {
            writeValue(out, dto.getValue(field));
        }
    }

    protected void writeValue(JsonWriter out, Object value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else if (value instanceof Boolean) {
            out.value((Boolean) value);
        } else if (value instanceof Date) {
            out.value(DateTimeFormatter.ISO_INSTANT.format(((Date) value).toInstant()));
        } else if (value instanceof Number) {
            out.value((Number) value);
        } else if (value instanceof String) {
            out.value((String) value);
        } else {
            out.value(value.toString());
        }
    }

}
