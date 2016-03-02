package io.aiur.oss.db.jdbc.jdbc.annotation;

import java.lang.annotation.*;

/**
 * Created by dave on 12/15/15.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JdbcColumn {

    String value();

}