package io.github.xrickastley.sevenelements.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fabricmc.api.EnvType;

/**
 * Applied to declare that the annotated element only works as intended in the specified environment. <br> <br>
 *
 * This indicates that the annotated element, while present in both environments, should as much as
 * possible, only be used in the specified environment, as its usage in different environments may
 * yield to unexpected behavior different from the documented behavior. <br> <br>
 *
 * This annotation only <i>discourages</i> usage in non-specified environments due to possible
 * unexpected behaviors present in the non-specified environment, as the annotated element stil
 * exists in both environments.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface ExpectedEnvironment {
	/**
	 * Returns the environment type that the annotated element works as intended in.
	 */
	EnvType value();
}
