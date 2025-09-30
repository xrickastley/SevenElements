package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;

public final class FrozenStatusEffect extends ElementalStatusEffect {
	FrozenStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9, Element.FREEZE);

		this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, SevenElements.identifier("frozen"), -1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, SevenElements.identifier("frozen"), Integer.MIN_VALUE, EntityAttributeModifier.Operation.ADD_VALUE);
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

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.getStatusEffect(SevenElementsStatusEffects.FROZEN).getDuration() == 1 && entity instanceof final MobEntity mob)
			mob.setAiDisabled(false);

		return true;
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
