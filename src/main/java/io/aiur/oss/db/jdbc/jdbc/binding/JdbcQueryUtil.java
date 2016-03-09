package io.aiur.oss.db.jdbc.jdbc.binding;

import io.aiur.oss.db.jdbc.jdbc.annotation.JdbcQuery;
import io.aiur.oss.db.jdbc.jdbc.mapping.SqlCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by dave on 3/9/16.
 */
@Slf4j
public class JdbcQueryUtil {

    public static StringBuilder sqlBuilderFromMethod(Method method, SqlCache sqlCache, boolean required) {
        String clazz = method.getDeclaringClass().getSimpleName();
        JdbcQuery ann = method.getAnnotation(JdbcQuery.class);

        if( ann == null ){
            if( required ){
                log.warn("Could not determine @JdbcQuery for {}#{}",
                        clazz, method.getName());
                throw new IllegalArgumentException("Could not find @JdbcQuery for method " + clazz + "#" + method.getName());
            }else{
                return null;
            }
        }

        StringBuilder sql = new StringBuilder();
        if( StringUtils.hasText(ann.value()) ){
            sql.append( sqlCache.getByKey(ann.value()) );
        }else{
            sql.append( ann.query() );
        }
        if( sql == null && required){
            log.warn("Could not determine query for {}#{} with annotation {}",
                    clazz, method.getName(), ann);
            throw new IllegalArgumentException("Could not determine query for method " + clazz + "#" + method.getName());
        }
        return sql.length() == 0 ? null : sql;
    }

    public static String sqlFromMethod(Method method, SqlCache sqlCache, boolean required) {
        StringBuilder builder = sqlBuilderFromMethod(method, sqlCache, required);
        return builder == null ? null : builder.toString();
    }
}
