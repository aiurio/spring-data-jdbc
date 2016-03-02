package io.aiur.oss.db.jdbc.jdbc.annotation;

import com.google.common.base.CaseFormat;

import java.lang.annotation.*;

/**
 * Created by dave on 12/15/15.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JdbcColumnConvert {

    CaseFormat value() default CaseFormat.LOWER_UNDERSCORE;

}