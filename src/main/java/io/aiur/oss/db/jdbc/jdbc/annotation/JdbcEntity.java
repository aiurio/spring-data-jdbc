package io.aiur.oss.db.jdbc.jdbc.annotation;

import java.lang.annotation.*;

/**
 * Created by dave on 12/15/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JdbcEntity {

    String table() default "";
    String schema() default "";
}