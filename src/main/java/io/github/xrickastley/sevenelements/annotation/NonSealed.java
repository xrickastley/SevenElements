package io.github.xrickastley.sevenelements.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that the annotated element must be considered {@code non-sealed}.
 *
 * <p>This indicates that the annotated element may be freely extended, implemented or
 * overridden by other subtypes.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
public @interface NonSealed {}
