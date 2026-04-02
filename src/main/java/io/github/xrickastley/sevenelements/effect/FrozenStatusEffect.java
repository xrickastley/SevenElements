package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class FrozenStatusEffect extends ElementalStatusEffect {
	FrozenStatusEffect() {
		super(MobEffectCategory.HARMFUL, 0x84e8f9, Element.FREEZE);

		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SevenElements.identifier("frozen"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		this.addAttributeModifier(Attributes.ATTACK_DAMAGE, SevenElements.identifier("frozen"), Integer.MIN_VALUE, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		super.onEffectStarted(entity, amplifier);

		FrozenEffectComponent.KEY.get(entity).freeze();
	}

	@Override
	public void onRemoved(LivingEntity entity, int amplifier) {
		super.onRemoved(entity, amplifier);

		FrozenEffectComponent.KEY.get(entity).unfreeze();
	}

	@Override
	public boolean applyEffectTick(ServerLevel serverWorld, LivingEntity entity, int amplifier) {
		if (entity.getEffect(SevenElementsStatusEffects.FROZEN).getDuration() == 1 && entity instanceof final Mob mob)
			mob.setNoAi(false);

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}
