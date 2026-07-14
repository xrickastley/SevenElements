package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.interfaces.SevenElementsOrderedSubmitNodeCollector;
import io.github.xrickastley.sevenelements.interfaces.SevenElementsSubmitNodeCollection;
import io.github.xrickastley.sevenelements.renderer.feature.CrystallizeShieldFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer.ElementState;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer.ElementGaugeState;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Ease;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.util.Mth;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin implements SevenElementsSubmitNodeCollection, SevenElementsOrderedSubmitNodeCollector {
	@Shadow
	@Final
	public SimpleFeatureRenderPhase translucentCustomGeometry;

	@Unique
	private final SimpleFeatureRenderPhase sevenelements$elements = new SimpleFeatureRenderPhase();
	@Unique
	private final SimpleFeatureRenderPhase sevenelements$elementGauges = new SimpleFeatureRenderPhase();
	@Unique
	private final SimpleFeatureRenderPhase sevenelements$crystalizeShields = new SimpleFeatureRenderPhase();

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"
		)
	)
	private <E> List<E> addToAllPhases(List<E> original) {
		final List<E> allPhases = new ArrayList<>(original);

		allPhases.addAll(allPhases.indexOf(this.translucentCustomGeometry), ClassInstanceUtil.cast(List.of(this.sevenelements$elements, this.sevenelements$elementGauges, this.sevenelements$crystalizeShields)));

		return Collections.unmodifiableList(allPhases);
	}

	@Unique
	@Override
	public SimpleFeatureRenderPhase sevenelements$getElements() {
		return this.sevenelements$elements;
	}

	@Unique
	@Override
	public SimpleFeatureRenderPhase sevenelements$getElementGauges() {
		return this.sevenelements$elementGauges;
	}

	@Unique
	@Override
	public SimpleFeatureRenderPhase sevenelements$getCrystallizeShields() {
		return this.sevenelements$crystalizeShields;
	}

	@Unique
	@Override
	public void sevenelements$submitElement(PoseStack stack, ElementState elementState, Camera camera, float xOffset, float yOffset) {
		final float blinkInterval = ElementFeatureRenderer.BLINK_SECONDS / ElementFeatureRenderer.BLINK_COUNT;
		final float intervalSplit = blinkInterval / 2f;

		stack.pushPose();
		stack.translate(0, yOffset * 1.1, 0);
		stack.mulPose(new Matrix4f().rotation(camera.rotation()));
		stack.scale(0.50F, 0.50F, 0.50F);

		final double blinkProgress = elementState.secondsLeft() % blinkInterval;
		final float alpha = (float) (elementState.secondsLeft() <= (ElementFeatureRenderer.BLINK_SECONDS + intervalSplit)
			? blinkProgress <= intervalSplit
				? Mth.lerp(blinkProgress / intervalSplit, 0, 1)
				: Mth.lerp(((blinkProgress) - 0.25) / intervalSplit, 1, 0)
			: 1);

		this.sevenelements$elements.submit(
			new ElementFeatureRenderer.Submit(stack.last().copy().pose(), elementState.element(), xOffset, alpha)
		);

		if (elementState.appliedForTicks() <= 5) {
			final double animationProgress = Ease.LINEAR.applyLerpProgress(elementState.appliedForTicks() + elementState.tickDelta(), 1, 6);
			final float scale2 = (float) (animationProgress * 2);
			final float alpha2 = (float) (1 - (animationProgress * 0.5));

			stack.scale(scale2, scale2, scale2);

			this.sevenelements$elements.submit(
				new ElementFeatureRenderer.Submit(stack.last().copy().pose(), elementState.element(), xOffset, alpha2)
			);
		}

		stack.popPose();
	}

	@Unique
	@Override
	public void sevenelements$submitElementalGauge(PoseStack stack, ElementGaugeState gaugeState, Camera camera, float xOffset, float yOffset) {
		stack.pushPose();
		stack.translate(0f, yOffset * 1.15, 0f);
		stack.mulPose(new Matrix4f().rotation(camera.rotation()));
		stack.scale(ElementGaugeFeatureRenderer.GAUGE_SCALE, ElementGaugeFeatureRenderer.GAUGE_SCALE * 0.5f, ElementGaugeFeatureRenderer.GAUGE_SCALE);

		this.sevenelements$elementGauges.submit(
			new ElementGaugeFeatureRenderer.Submit(stack.last().copy(), gaugeState, xOffset, yOffset)
		);

		stack.popPose();
	}

	@Unique
	@Override
	public void sevenelements$submitCrystallizeShield(PoseStack stack, Element shieldElement, Camera camera, float yLength) {
		stack.pushPose();
		stack.mulPose(Axis.YN.rotationDegrees(camera.yRot()));
		stack.translate(0, yLength * 0.6, 0);

		this.sevenelements$crystalizeShields.submit(
			new CrystallizeShieldFeatureRenderer.Submit(stack.last().copy(), shieldElement, yLength)
		);

		stack.popPose();
	}
}
