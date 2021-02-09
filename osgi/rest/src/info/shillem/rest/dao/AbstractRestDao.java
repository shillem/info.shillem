package info.shillem.rest.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import info.shillem.dao.QuerySerializer;
import info.shillem.dao.lang.DaoErrorCode;
import info.shillem.dao.lang.DaoException;
import info.shillem.dto.AttachedFilesSerializer;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseDtoDeserializer;
import info.shillem.dto.BaseDtoSerializer;
import info.shillem.dto.BaseField;
import info.shillem.rest.factory.RestFactory;
import info.shillem.util.JsonHandler;

public abstract class AbstractRestDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    protected static final JsonHandler JSON = new JsonHandler(new ObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(
                    new SimpleModule()
                            .addSerializer(new BaseDtoSerializer())
                            .addSerializer(new AttachedFilesSerializer())
                            .addSerializer(new QuerySerializer())
                            .setDeserializerModifier(new BaseDtoDeserializer.Modifier()))
            .setSerializationInclusion(Include.NON_NULL));

    protected final RestFactory factory;

    protected AbstractRestDao(RestFactory factory) {
        this.factory = Objects.requireNonNull(factory, "Factory cannot be null");
    }

    protected <R> R consume(
            HttpUriRequest request,
            BiFunction<HttpResponse, HttpEntity, R> fn)
            throws DaoException {
        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            R value = fn.apply(response, entity);

            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                // Do nothing
            }

            return value;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected DaoException wrappedBadResponse(int statusCode, HttpEntity entity) {
        try {
            DaoException e = new DaoException(DaoErrorCode.GENERIC);

            e.setProperty("statusCode", statusCode);

            if ("application/json".equals(entity.getContentType().getValue())) {
                e.setProperty("content", JSON.deserialize(entity.getContent()));
            } else {
                InputStreamReader isr =
                        new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8);

                e.setProperty("content",
                        new BufferedReader(isr).lines().collect(Collectors.joining("\n")));
            }

            return e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
