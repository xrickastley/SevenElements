package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public final class SuperconductStatusEffect extends StatusEffect {
	SuperconductStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0xbcb0ff);

		this.addAttributeModifier(SevenElementsAttributes.PHYSICAL_RES, "b2533846-97fe-447e-a9ed-d4d83b480e67", -40, Operation.ADDITION);
	}
}
