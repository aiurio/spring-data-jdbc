package io.aiur.oss.db.jdbc.jdbc.convert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import other.MapUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by dave on 2/10/16.
 */
public class JodaJdbcTypeConverter implements JdbcTypeConverter{

    private List<Class<?>> TYPES = Lists.newArrayList(
        DateTime.class
    );


    @Override
    public boolean canConvertToSqlType(Object raw) {
        return raw != null && TYPES.contains(raw.getClass());
    }

    @Override
    public Object convertToSqlType(Object raw) {
        switch(raw.getClass().getSimpleName() ){
            case "DateTime":
                return new Timestamp(((DateTime) raw).getMillis() );

        }
        throw new IllegalStateException("Could not match object " + raw);
    }

    @Override
    public int getOrder() {
        return -10;
    }

}
