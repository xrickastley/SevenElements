package io.github.xrickastley.sevenelements.annotation.mixin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that the annotated parameter is a local value originating from
 * the method. <br> <br>
 *
 * No validations are held for these "mixin injectors", and are only annotated to indicate that the
 * method is a "pseudo-mixin injector". These injectors are also applied directly into the source
 * code and can be seen in the compiled source, not at runtime like a normal Mixin injector.
 *
 * @see com.llamalad7.mixinextras.sugar.Local
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.PARAMETER })
public @interface Local {
	public int ordinal() default -1;

	public boolean argsOnly() default false;

	public String field() default "";

	public boolean self() default false;
}
