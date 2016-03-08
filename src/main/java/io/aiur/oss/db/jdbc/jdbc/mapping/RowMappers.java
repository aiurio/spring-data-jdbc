package io.aiur.oss.db.jdbc.jdbc.mapping;

import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcMarshallers;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.MissingRowUnmapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.RowUnmapper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dave on 3/8/16.
 */
public class RowMappers {


    private static final List<Class<?>> PRIMITIVES = Arrays.asList(
            Long.class, Integer.class, Float.class, Double.class, String.class, Number.class, Boolean.class
    );


    public static <T> RowMapper<T> resolveRowMapper(Class<T> type){
        JdbcMarshallers a = AnnotationUtils.findAnnotation(type, JdbcMarshallers.class);
        if( a != null ){
            try {
                RowMapper instance = a.mapper().newInstance();
                if( instance instanceof BeanPropertyRowMapper){
                    ((BeanPropertyRowMapper) instance).setMappedClass(type);
                }
                return instance;
            } catch (Exception e){
                throw new RuntimeException("Failed instantiating RowMapper for " + type.getName(), e);
            }
        }

        if( PRIMITIVES.contains(type) ){
            return new SingleColumnRowMapper<>(type);
        }

        return new ColumnAwareBeanPropertyRowMapper<>(type);
    }

    public static <T> RowUnmapper<T> resolveRowUnmapper(Class<T> type){
        JdbcMarshallers a = AnnotationUtils.findAnnotation(type, JdbcMarshallers.class);
        if( a != null ){
            try {
                RowUnmapper instance = a.unmapper().newInstance();
                return instance;
            } catch (Exception e){
                throw new RuntimeException("Failed instantiating RowUnmapper for " + type.getName(), e);
            }
        }

        return new MissingRowUnmapper<>();
    }
}
