package io.github.xrickastley.sevenelements.effect;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public final class SuperconductStatusEffect extends MobEffect {
	SuperconductStatusEffect() {
		super(MobEffectCategory.HARMFUL, 0xbcb0ff);

		this.addAttributeModifier(SevenElementsAttributes.PHYSICAL_RES, SevenElements.identifier("superconduct"), -40, Operation.ADD_VALUE);
	}
}
