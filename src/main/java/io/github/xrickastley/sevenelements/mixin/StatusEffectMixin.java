package io.github.xrickastley.sevenelements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.xrickastley.sevenelements.interfaces.EntityAwareEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin implements EntityAwareEffect {
	@Shadow
	public abstract StatusEffect addAttributeModifier(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation);

	public StatusEffect method_5566(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation) {
		return this.addAttributeModifier(attribute, uuid, amount, operation);
	}
}
