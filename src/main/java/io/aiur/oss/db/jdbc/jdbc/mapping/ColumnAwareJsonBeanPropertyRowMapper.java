package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.aiur.oss.db.jdbc.jdbc.convert.impl.joda.editor.JodaDateTimeEditor;
import io.aiur.oss.db.jdbc.jdbc.convert.impl.joda.editor.JodaLocalDateEditor;
import io.aiur.oss.db.jdbc.jdbc.convert.impl.joda.editor.JodaLocalDateTimeEditor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dave on 12/30/15.
 */
@Slf4j
public class ColumnAwareJsonBeanPropertyRowMapper<T> extends ColumnAwareBeanPropertyRowMapper<T> {

    @Inject @Lazy
    protected ObjectMapper objectMapper;

    @Getter
    private List<Tuple> jsonCollections = Lists.newArrayList();

    @Getter
    private List<Tuple> jsonObjects = Lists.newArrayList();

    @Getter
    private List<MapTuple> jsonMaps = Lists.newArrayList();

    public ColumnAwareJsonBeanPropertyRowMapper() { super(); }

    public ColumnAwareJsonBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public ColumnAwareJsonBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
        super(mappedClass, checkFullyPopulated);
    }

    public <E> ColumnAwareJsonBeanPropertyRowMapper jsonObject(Class<?> type, String property){
        jsonObjects.add(new Tuple(type, property, null));
        return this;
    }

    public <E> ColumnAwareJsonBeanPropertyRowMapper jsonCollection(Class<?> type, String property, TypeReference<List<E>> typeRef){
        jsonCollections.add(new Tuple(type, property, typeRef));
        return this;
    }

    public <K, V> ColumnAwareJsonBeanPropertyRowMapper jsonMap(Class<?> type, String property, TypeReference<Map<K, V>> typeRef){
        jsonMaps.add(new MapTuple(type, property, typeRef));
        return this;
    }

    @Override
    protected void initBeanWrapper(BeanWrapper bw) {
        super.initBeanWrapper(bw);
        initializeJoda(bw);

        jsonObjects.forEach(t -> {
            bw.registerCustomEditor(t.type, t.property, new JsonObjectDeserializer<>(t.type, objectMapper));
        });

        jsonCollections.forEach(t -> {
            bw.registerCustomEditor(t.type, t.property, new JsonCollectionDeserializer(t.typeRef, objectMapper));
        });

        jsonMaps.forEach(t -> {
            bw.registerCustomEditor(t.type, t.property, new JsonMapDeserializer(t.typeRef, objectMapper));
        });


    }

    protected void initializeJoda(BeanWrapper bw) {
        bw.registerCustomEditor(LocalDate.class, new JodaLocalDateEditor());
        bw.registerCustomEditor(DateTime.class, new JodaDateTimeEditor());
        bw.registerCustomEditor(LocalDateTime.class, new JodaLocalDateTimeEditor());
    }


    private class Tuple<E> {
        final Class<?> type;
        final String property;
        final TypeReference typeRef;

        private Tuple(Class<?> type, String property, TypeReference typeRef) {
            this.type = type;
            this.property = property;
            this.typeRef = typeRef;
        }
    }

    @RequiredArgsConstructor
    private class MapTuple<E> {
        final Class<?> type;
        final String property;
        final TypeReference<Map<?, ?>> typeRef;
    }

    @RequiredArgsConstructor
    class JsonCollectionDeserializer<E> extends PropertyEditorSupport {

        private final TypeReference<?> typeRef;
        private final ObjectMapper objectMapper;

        @Override
        public void setValue(Object value) {
            if(value == null) {

                // Handle collections that were returned as null
                if( ParameterizedType.class.isAssignableFrom(typeRef.getType().getClass()) ){
                    Class<?> type = (Class<?>) ((ParameterizedType) typeRef.getType()).getRawType();
                    if( List.class.isAssignableFrom(type) ){
                        value = Lists.newArrayList();
                    }else if( Set.class.isAssignableFrom(type) ){
                        value = Sets.newHashSet();
                    }
                }

            }else{
                // parse our result
                try {
                    Object r = objectMapper.readValue(value.toString(), typeRef);
                    value = r;
                } catch (IOException e) {
                    throw new RuntimeException("Could not deserialize collection of " + typeRef.getType().toString(), e);
                }
            }

            super.setValue(value);
        }

        // if the JSON is cast to a string, this gets called instead...
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(text);
        }
    }

    @RequiredArgsConstructor
    class JsonMapDeserializer<K, V> extends PropertyEditorSupport {

        private final TypeReference<Map<K, V>> typeRef;
        private final ObjectMapper objectMapper;

        @Override
        public void setValue(Object value) {
            if(value == null) {

                // Handle collections that were returned as null
                if( ParameterizedType.class.isAssignableFrom(typeRef.getType().getClass()) ){
                    Class<?> type = (Class<?>) ((ParameterizedType) typeRef.getType()).getRawType();
                    if( Map.class.isAssignableFrom(type) ){
                        value = Maps.newHashMap();
                    }
                }

            } else if( Map.class.isInstance(value) ){
                // do nothing... we're already converted!
            }else{
                // parse our result
                try {
                    Map<K, V> r = objectMapper.readValue(value.toString(), typeRef);
                    value = r;
                } catch (IOException e) {
                    throw new RuntimeException("Could not deserialize collection of " + typeRef.getType().toString(), e);
                }
            }
            super.setValue(value);
        }

        // Ignore the typecheck, to work around the Map<String, Object> scenario
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(text);
        }
    }


    @RequiredArgsConstructor
    class JsonObjectDeserializer<V> extends PropertyEditorSupport {

        private final Class<V> type;
        private final ObjectMapper objectMapper;

        @Override
        public void setValue(Object value) {
            if(value != null) {

                // parse our result
                try {
                    V r = objectMapper.readValue(value.toString(), type);
                    value = r;
                } catch (IOException e) {
                    throw new RuntimeException("Could not deserialize instance of " + type.toString(), e);
                }
            }
            super.setValue(value);
        }
    }
}
