package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class SuperconductStatusEffect extends StatusEffect {
	SuperconductStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0xbcb0ff);

		this.addAttributeModifier(SevenElementsAttributes.PHYSICAL_RES, SevenElements.identifier("superconduct"), -40, Operation.ADD_VALUE);
	}
}
