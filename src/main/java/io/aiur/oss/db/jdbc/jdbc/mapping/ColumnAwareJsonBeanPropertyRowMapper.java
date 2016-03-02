package io.aiur.oss.db.jdbc.jdbc.mapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;

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
public class ColumnAwareJsonBeanPropertyRowMapper<T> extends ColumnAwareBeanPropertyRowMapper<T> {

    @Inject
    private ObjectMapper objectMapper;

    private List<Tuple> jsonCollections = Lists.newArrayList();

    public ColumnAwareJsonBeanPropertyRowMapper() { super(); }

    public ColumnAwareJsonBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public ColumnAwareJsonBeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
        super(mappedClass, checkFullyPopulated);
    }

    public <E> ColumnAwareJsonBeanPropertyRowMapper jsonCollection(Class<?> type, String property, TypeReference<List<E>> typeRef){
        jsonCollections.add(new Tuple(type, property, typeRef));
        return this;
    }



    @Override
    protected void initBeanWrapper(BeanWrapper bw) {
        super.initBeanWrapper(bw);
        jsonCollections.forEach(t -> {
            bw.registerCustomEditor(t.type, t.property, new JsonCollectionDeserializer(t.typeRef, objectMapper));
        });
    }


    @RequiredArgsConstructor
    private class Tuple<E> {
        final Class<?> type;
        final String property;
        final TypeReference<List<?>> typeRef;
    }

    @RequiredArgsConstructor
    class JsonCollectionDeserializer<E> extends PropertyEditorSupport {

        private final TypeReference<List<E>> typeRef;
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
                    List<E> r = objectMapper.readValue(value.toString(), typeRef);
                    value = r;
                } catch (IOException e) {
                    throw new RuntimeException("Could not deserialize collection of " + typeRef.getType().toString(), e);
                }
            }

            super.setValue(value);
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
    }


}
