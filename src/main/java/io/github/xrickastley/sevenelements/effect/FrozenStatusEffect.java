package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class FrozenStatusEffect extends ElementalStatusEffect {
	FrozenStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9, Element.FREEZE);

		this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "26fbc919-06ff-4775-aaf0-e3a80fd045d0", -1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
		this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "dcaa96b5-a028-452a-9de0-c901e5b52e21", Integer.MIN_VALUE, EntityAttributeModifier.Operation.ADDITION);
	}

	@Override
	public void onApplied(LivingEntity entity, int amplifier) {
		super.onApplied(entity, amplifier);

		FrozenEffectComponent.KEY.get(entity).freeze();
	}

	@Override
	public void onRemoved(LivingEntity entity, int amplifier) {
		super.onRemoved(entity, amplifier);

		FrozenEffectComponent.KEY.get(entity).unfreeze();
	}
}
