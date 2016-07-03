package io.aiur.oss.db.jdbc.jdbc.convert.impl.joda.converter;

import com.google.common.collect.Lists;
import io.aiur.oss.db.jdbc.jdbc.convert.JdbcTypeConverter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by dave on 2/10/16.
 */
public class JodaJdbcTypeConverter implements JdbcTypeConverter {

    private List<Class<?>> TYPES = Lists.newArrayList(
            DateTime.class, LocalDate.class
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
            case "LocalDate":
                return new java.sql.Date( ((LocalDate) raw).toDate().getTime() );
            case "LocalTime":
                return new Time( ((LocalTime) raw).toDateTimeToday().getMillis());

        }
        throw new IllegalStateException("Could not match object " + raw);
    }

    @Override
    public int getOrder() {
        return -10;
    }

}
