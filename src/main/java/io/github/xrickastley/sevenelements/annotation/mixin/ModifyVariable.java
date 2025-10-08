package io.github.xrickastley.sevenelements.annotation.mixin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that the annotated method is considered an "injected" method originating from
 * this class that modifies a local variable. <br> <br>
 *
 * No validations are held for these "mixin injectors", and are only annotated to indicate that the
 * method is a "pseudo-mixin injector". These injectors are also applied directly into the source
 * code and can be seen in the compiled source, not at runtime like a normal Mixin injector.
 *
 * @see org.spongepowered.asm.mixin.injection.ModifyVariable
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
public @interface ModifyVariable {
	public String[] method() default {};

	public At at();
}
