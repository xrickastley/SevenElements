package io.github.xrickastley.sevenelements.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer.ElementState;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer.ElementGaugeState;

import net.minecraft.client.Camera;

public interface SevenElementsOrderedSubmitNodeCollector {
	default void sevenelements$submitElement(PoseStack stack, ElementState elementState, Camera camera, float xOffset, float yOffset) {}

	default void sevenelements$submitElementalGauge(PoseStack stack, ElementGaugeState gaugeState, Camera camera, float xOffset, float yOffset, float gaugeYOffset) {}

	default void sevenelements$submitCrystallizeShield(PoseStack stack, Element shieldElement, Camera camera, float yLength) {}
}
