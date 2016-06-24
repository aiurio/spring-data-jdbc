package io.aiur.oss.db.jdbc.jdbc.convert.impl.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiur.oss.db.jdbc.jdbc.convert.JdbcTypeConverter;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by dave on 6/10/16.
 */
public class PostgresJsonJdbcTypeConverter implements JdbcTypeConverter{

    @Inject @Lazy
    private ObjectMapper objectMapper;

    @Override
    public boolean canConvertToSqlType(Object rawValue) {
        return Map.class.isInstance(rawValue);
    }

    @Override
    public Object convertToSqlType(Object rawValue) {
        try {
            String json = objectMapper.writeValueAsString(rawValue);
            PGobject o = new PGobject();
            o.setType("json");
            o.setValue(json);
            return o;
        } catch (Exception e) {
            throw new RuntimeException("Failed converting object to Postgres JSON type", e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
