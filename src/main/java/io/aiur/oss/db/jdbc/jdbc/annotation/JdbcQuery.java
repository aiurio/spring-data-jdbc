package io.aiur.oss.db.jdbc.jdbc.annotation;

import java.lang.annotation.*;

/**
 * Assumptions on query:
 *   - No Limit / Offsets
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JdbcQuery {

    /**
     * A key to lookup from the cache
     * @return
     */
    String value() default "";

    String countKey() default "";

    /**
     * A custom in-line query, with values being substituted similar to SD JPA
     * (?0 = first param, ?1 = secondParam)
     *
     * @return
     */
    String query() default "";
    String countQuery() default "";

    Class<?> beanType() default Class.class;

    Class<?> projection() default Class.class;

}