package io.github.xrickastley.sevenelements.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer.ElementState;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer.ElementGaugeState;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;

@Mixin(SubmitNodeStorage.class)
public abstract class SubmitNodeStorageMixin implements SubmitNodeCollector {
	@Unique
	@Override
	public void sevenelements$submitElement(PoseStack stack, ElementState elementState, Camera camera, float xOffset, float yOffset) {
		this.order(0).sevenelements$submitElement(stack, elementState, camera, xOffset, yOffset);
	}

	@Unique
	@Override
	public void sevenelements$submitElementalGauge(PoseStack stack, ElementGaugeState gaugeState, Camera camera, float xOffset, float yOffset, float gaugeYOffset) {
		this.order(0).sevenelements$submitElementalGauge(stack, gaugeState, camera, xOffset, yOffset, gaugeYOffset);
	}

	@Unique
	@Override
	public void sevenelements$submitCrystallizeShield(PoseStack stack, Element shieldElement, Camera camera, float yLength) {
		this.order(0).sevenelements$submitCrystallizeShield(stack, shieldElement, camera, yLength);
	}
}
