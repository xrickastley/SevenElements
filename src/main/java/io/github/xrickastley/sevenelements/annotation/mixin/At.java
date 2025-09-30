package io.github.xrickastley.sevenelements.annotation.mixin;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ /* No targets allowed */ })
public @interface At {
    public enum Shift {
        NONE, BEFORE, AFTER, BY;
    }

    public String target() default "";

    public String value();

    public int ordinal() default -1;

    public Shift shift() default Shift.NONE;
}
