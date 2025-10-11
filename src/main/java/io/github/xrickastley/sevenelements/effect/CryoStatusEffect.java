package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public final class CryoStatusEffect extends ElementalStatusEffect {
	CryoStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9, Element.CRYO);

		this.addAttributeModifier(EntityAttributes.MOVEMENT_SPEED, SevenElements.identifier("cryo"), -0.15, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		this.addAttributeModifier(EntityAttributes.ATTACK_DAMAGE, SevenElements.identifier("cryo"), -0.15, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	@Override
	public boolean applyUpdateEffect(ServerWorld serverWorld, LivingEntity entity, int amplifier) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component.hasElementalApplication(Element.CRYO)) return false;

		entity.removeStatusEffect(SevenElementsStatusEffects.CRYO);

		return true;
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
