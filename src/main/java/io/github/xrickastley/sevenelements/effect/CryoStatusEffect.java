package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class CryoStatusEffect extends ElementalStatusEffect {
	CryoStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x84e8f9, Element.CRYO);

		this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "88e8f74f-adad-49c0-9138-76789ec737a5", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
		this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "96b29a19-d2c9-45fc-8bd4-6f624b6d309e", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		if (component.hasElementalApplication(Element.CRYO)) return;

		entity.removeStatusEffect(SevenElementsStatusEffects.CRYO);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
