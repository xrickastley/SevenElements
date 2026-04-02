package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class CryoStatusEffect extends ElementalStatusEffect {
	CryoStatusEffect() {
		super(MobEffectCategory.HARMFUL, 0x84e8f9, Element.CRYO);

		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SevenElements.identifier("cryo"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		this.addAttributeModifier(Attributes.ATTACK_DAMAGE, SevenElements.identifier("cryo"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	@Override
	public boolean applyEffectTick(ServerLevel serverWorld, LivingEntity entity, int amplifier) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component.hasElementalApplication(Element.CRYO)) return false;

		entity.removeEffect(SevenElementsStatusEffects.CRYO);

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}
