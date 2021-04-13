package info.shillem.dto;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import info.shillem.util.Unthrow;

public class BaseDtoDeserializer<T extends BaseDto<?>> extends StdDeserializer<T> {

    public static class Modifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyDeserializer(
                DeserializationConfig config,
                BeanDescription desc,
                JsonDeserializer<?> deserializer) {
            if (BaseDto.class.isAssignableFrom(desc.getBeanClass())) {
                return new BaseDtoDeserializer<>(desc.getBeanClass());
            }

            return deserializer;
        }
    }

    private static final long serialVersionUID = 1L;

    private BaseDtoDeserializer(Class<?> t) {
        super(t);
    }

    @Override
    public T deserialize(
            JsonParser parser,
            DeserializationContext context)
            throws IOException, JsonProcessingException {
        JsonNode node = parser.readValueAsTree();

        try {
            @SuppressWarnings("unchecked")
            T instance = (T) handledType().getConstructor().newInstance();

            if (node.has("id")) {
                instance.setId(node.get("id").asText());
            }

            if (node.has("lastModified")) {
                instance.setLastModified(deserializeDate(node.get("lastModified").asText()));
            }

            if (node.has("databaseUrl")) {
                instance.setDatabaseUrl(node.get("databaseUrl").asText());
            }

            node.get("values").fields().forEachRemaining((entry) -> Unthrow.on(() -> {
                deserializeField(
                        instance.fieldOf(entry.getKey()),
                        entry.getValue(),
                        instance,
                        parser.getCodec());
            }));

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Date deserializeDate(String value) {
        return Date.from(ZonedDateTime.parse(value).toInstant());
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E> & BaseField> void deserializeField(
            E field,
            JsonNode node,
            BaseDto<?> value,
            ObjectCodec codec)
            throws IOException {
        BaseDto<E> dto = ((BaseDto<E>) value);
        ValueType props = field.getValueType();

        if (props.isCollection()) {
            Iterator<JsonNode> iter = node.iterator();
            Collection<Object> values = props.newCollection();

            while (iter.hasNext()) {
                values.add(iter.next().traverse(codec).readValueAs(props.getValueClass()));
            }

            dto.setValue(field, values);
        } else {
            dto.setValue(field, node.traverse(codec).readValueAs(props.getValueClass()));
        }
    }

}
