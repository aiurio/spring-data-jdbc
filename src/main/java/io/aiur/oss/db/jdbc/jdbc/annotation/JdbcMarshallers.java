package io.aiur.oss.db.jdbc.jdbc.annotation;

import io.aiur.oss.db.jdbc.jdbc.mapping.ColumnAwareBeanPropertyRowMapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.MissingRowUnmapper;
import io.aiur.oss.db.jdbc.jdbc.nurkiewicz.RowUnmapper;
import org.springframework.jdbc.core.RowMapper;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JdbcMarshallers {

    Class<? extends RowMapper> mapper() default ColumnAwareBeanPropertyRowMapper.class;
    Class<? extends RowUnmapper> unmapper() default MissingRowUnmapper.class;

}
