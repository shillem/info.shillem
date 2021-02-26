package info.shillem.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import info.shillem.dao.lang.DaoException;
import info.shillem.dao.lang.DaoRecordException;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseDto.SchemaFilter;
import info.shillem.dto.BaseField;

public class Cycle<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    public class Cursor {

        private final T record;
        private final List<String> tags = new ArrayList<>();

        Cursor(T record) {
            this.record = record;
        }

        public T getRecord() {
            return record;
        }

        public boolean isTagged(String name) {
            return tags.contains(name);
        }

        public void tag(String name) {
            tags.add(name);
        }

        public Object validate(E field) throws DaoException {
            Object value = record.getValue(field);

            if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                throw DaoRecordException.asMissingField(field);
            }

            return value;
        }

        public void validate(Set<E> fields) throws DaoException {
            validate(fields, null);
        }

        public void validate(Set<E> fields, Consumer<E> fn) throws DaoException {
            for (E field : fields) {
                if (fn != null) {
                    fn.accept(field);
                } else {
                    validate(field);
                }
            }
        }

    }

    private enum Phase {
        CHANGE, CREATE, CREATE_VALIDATE, UPDATE, UPDATE_VALIDATE, UPSERT, UPSERT_VALIDATE;
    }

    private final Map<Phase, List<Consumer<Cursor>>> phases = new HashMap<>();
    private final Map<Object, List<BiConsumer<Cursor, E>>> changePhases = new HashMap<>();

    public void onChange(BaseField field, BiConsumer<Cursor, E> consumer) {
        changePhases.computeIfAbsent(field, (k) -> new ArrayList<>()).add(consumer);
    }

    public void onChange(Class<? extends Serializable> cls, BiConsumer<Cursor, E> consumer) {
        changePhases.computeIfAbsent(cls, (k) -> new ArrayList<>()).add(consumer);
    }

    public void onCreate(Consumer<Cursor> consumer) {
        onPhase(Phase.CREATE, consumer);
    }

    public void onCreateValidate(Consumer<Cursor> consumer) {
        onPhase(Phase.CREATE_VALIDATE, consumer);
    }

    private void onPhase(Phase phase, Consumer<Cursor> consumer) {
        phases.computeIfAbsent(phase, (k) -> new ArrayList<>()).add(consumer);
    }

    public void onUpdate(Consumer<Cursor> consumer) {
        onPhase(Phase.UPDATE, consumer);
    }

    public void onUpdateValidate(Consumer<Cursor> consumer) {
        onPhase(Phase.UPDATE_VALIDATE, consumer);
    }

    public void onUpsert(Consumer<Cursor> consumer) {
        onPhase(Phase.UPSERT, consumer);
    }

    public void onUpsertValidate(Consumer<Cursor> consumer) {
        onPhase(Phase.UPSERT_VALIDATE, consumer);
    }

    public Cursor run(T record) {
        Cursor cursor = new Cursor(record);

        Optional.ofNullable(phases.get(record.isNew()
                ? Phase.CREATE
                : Phase.UPDATE))
                .ifPresent(list -> list.forEach(consumer -> consumer.accept(cursor)));

        Optional.ofNullable(phases.get(Phase.UPSERT))
                .ifPresent(list -> list.forEach(consumer -> consumer.accept(cursor)));

        Optional.ofNullable(phases.get(record.isNew()
                ? Phase.CREATE_VALIDATE
                : Phase.UPDATE_VALIDATE))
                .ifPresent(list -> list.forEach(consumer -> consumer.accept(cursor)));

        Optional.ofNullable(phases.get(Phase.UPSERT_VALIDATE))
                .ifPresent(list -> list.forEach(consumer -> consumer.accept(cursor)));

        record.getSchema(SchemaFilter.UPDATED).forEach(field -> {
            Stream.of(
                    changePhases.get(field),
                    changePhases.get(field.getProperties().getClass()))
                    .filter(Objects::nonNull)
                    .forEach(list -> list.forEach(consumer -> consumer.accept(cursor, field)));
        });

        return cursor;
    }

}
