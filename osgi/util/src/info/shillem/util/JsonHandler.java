package info.shillem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonHandler {
    
    private final ObjectMapper mapper;

    public JsonHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    public JsonNode deserialize(InputStream stream) {
        try {
            return mapper.readTree(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(InputStream stream, Class<T> cls) {
        try {
            return mapper.readValue(stream, cls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(InputStream stream, JavaType type) {
        try {
            return mapper.readValue(stream, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JsonNode deserialize(String value) {
        try {
            return mapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public <T> T deserialize(String value, Class<T> cls) {
        try {
            return mapper.readValue(value, cls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(String value, JavaType type) {
        try {
            return mapper.readValue(value, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public TypeFactory getTypeFactory() {
        return mapper.getTypeFactory();
    }

    public String serialize(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void serialize(Writer writer, Object value) throws IOException {
        try {
            mapper.writeValue(writer, value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
