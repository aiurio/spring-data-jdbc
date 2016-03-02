package io.aiur.oss.db.jdbc.annotation;

import java.lang.annotation.*;

/**
 * Created by dave on 1/6/16.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExposeId {}
