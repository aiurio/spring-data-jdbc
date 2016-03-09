package io.aiur.oss.db.jdbc.jdbc.convert;

import io.aiur.oss.db.jdbc.jdbc.mapping.ColumnAwareBeanPropertyRowMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by kalebscholes on 3/9/16.
 */
public class EnumJdbcTypeConverter implements JdbcTypeConverter {

    /**
     * See {@link ColumnAwareBeanPropertyRowMapper#offsetEnumIndex}.
     */
    @Getter
    @Setter
    private boolean offsetEnumIndex = true;

    @Override
    public boolean canConvertToSqlType(Object rawValue) {
        return rawValue != null && rawValue.getClass().isEnum();
    }

    @Override
    public Object convertToSqlType(Object rawValue) {
        int offset = offsetEnumIndex ? 1 : 0;
        return  ((Enum)rawValue).ordinal() + offset;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
