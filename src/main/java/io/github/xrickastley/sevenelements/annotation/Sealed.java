package io.github.xrickastley.sevenelements.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that the annotated element must be considered {@code sealed}.
 *
 * <p>This indicates that the annotated element <b>must not be extended, implemented or
 * overridden</b> by other subtypes except by those indicated inside {@code value}, which are
 * permitted to do so.
 *
 * <p>Like {@code sealed}, every permitted subtype must explicitly choose how it handles further
 * inheritance by either
 * <ul>
 * 	<li>marking the element as {@code final},</li>
 * 	<li>annotating the element with {@code @Sealed} and permitting its own subtypes, or</li>
 * 	<li>annotating the element with {@code @NonSealed}, allowing open inheritance.</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
public @interface Sealed {
	/**
	 * Returns the types that the annotated element allows overriding for.
	 */
	Class<?>[] value();
}
