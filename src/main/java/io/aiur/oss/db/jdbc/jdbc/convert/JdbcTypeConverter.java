package io.aiur.oss.db.jdbc.jdbc.convert;

import org.springframework.core.Ordered;

/**
 * Created by dave on 2/10/16.
 */
public interface JdbcTypeConverter extends Ordered {

    boolean canConvertToSqlType(Object rawValue);
    Object convertToSqlType(Object rawValue);

}
