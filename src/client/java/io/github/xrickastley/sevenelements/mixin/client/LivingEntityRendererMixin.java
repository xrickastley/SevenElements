package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.renderer.feature.ElementFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.feature.ElementGaugeFeatureRenderer;
import io.github.xrickastley.sevenelements.renderer.genshin.SpecialEffectsRenderer;
import io.github.xrickastley.sevenelements.util.ClientConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {
	protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
		super(context);

		throw new AssertionError();
	}

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void addSevenElementsLivingEntityRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
		livingEntityRenderState.sevenelements$fillRenderState(livingEntity, f);
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("TAIL")
	)
	private void addRenderers(S state, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (state.sevenelements$isDead()) return;

		this.sevenelements$renderElements(state, orderedRenderCommandQueue, matrixStack);
		this.sevenelements$renderElementalGauges(state, orderedRenderCommandQueue, matrixStack);
		this.sevenelements$renderCrystallizeShield(state, orderedRenderCommandQueue, matrixStack);
	}

	@Unique
	private void sevenelements$renderElements(final S entityState, final OrderedSubmitNodeCollector orderedRenderCommandQueue, final PoseStack matrixStack) {
		final List<ElementFeatureRenderer.ElementState> elementStates = entityState.sevenelements$getElementStates();

		if (elementStates.isEmpty()) return;

		final int length = 1;
		final int amount = elementStates.size();

		final double totalDistance = length * (amount - 1);
		final double offset = totalDistance / 2;

		final ArrayList<Vec3> result = new ArrayList<>();
		double curDistance = offset;
		for (int i = 0; i < amount; i++) {
			result.add(new Vec3(0, 0, curDistance));

			curDistance -= length;
		}

		final Iterator<Vec3> coords = result.iterator();

		elementStates
			.stream()
			.map(state -> new Pair<>(state, (float) coords.next().z()))
			.forEachOrdered(statePair -> orderedRenderCommandQueue.sevenelements$submitElement(matrixStack, statePair.getFirst(), entityRenderDispatcher.camera, statePair.getSecond(), (float) entityState.sevenelements$getBoundingBoxLength().y()));
	}

	@Unique
	private void sevenelements$renderElementalGauges(final S entityState, final OrderedSubmitNodeCollector orderedRenderCommandQueue, final PoseStack matrixStack) {
		final ClientConfig config = ClientConfig.get();
		final List<ElementGaugeFeatureRenderer.ElementGaugeState> gaugeStates = entityState.sevenelements$getGaugeStates();

		if (!config.developer.displayElementalGauges || gaugeStates.isEmpty()) return;

		final int elementCount = gaugeStates.size();
		final Iterator<ElementGaugeFeatureRenderer.ElementGaugeState> stateIterator = gaugeStates.iterator();

		Stream
			.iterate(0.0f, n -> (n / 1.25f) < elementCount, n -> n + 1.25f)
			.map(yOffset -> new Pair<>(stateIterator.next(), yOffset - 0.5f))
			.forEachOrdered(statePair -> orderedRenderCommandQueue.sevenelements$submitElementalGauge(matrixStack, statePair.getFirst(), entityRenderDispatcher.camera, (float) entityState.sevenelements$getBoundingBoxLength().x(), (float) entityState.sevenelements$getBoundingBoxLength().y(), statePair.getSecond() - 0.5f));
	}

	@Unique
	private void sevenelements$renderCrystallizeShield(final S state, final OrderedSubmitNodeCollector orderedRenderCommandQueue, final PoseStack matrixStack) {
		if (!SpecialEffectsRenderer.shouldRender(state) || state.sevenelements$getCrystallizeShieldElement() == null) return;

		final double lengthY = state.sevenelements$getBoundingBoxLength().y();

		orderedRenderCommandQueue.sevenelements$submitCrystallizeShield(matrixStack, state.sevenelements$getCrystallizeShieldElement(), entityRenderDispatcher.camera, (float) lengthY);
	}

	@Unique
	private <R> R sevenelements$ifFrozen(LivingEntity entity, Function<FrozenEffectComponent, R> ifFrozen, R ifNotFrozen) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		return component.isFrozen()
			? ifFrozen.apply(component)
			: ifNotFrozen;
	}

	@ModifyExpressionValue(
		method = "getRenderType",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Lnet/minecraft/resources/Identifier;"
		)
	)
	private Identifier renderFrostedModel(Identifier original, @Local(argsOnly = true) LivingEntityRenderState state) {
		return ClientConfig.getEffectRenderType().allowsSpecialEffects() && state.sevenelements$isFrozen()
			? Identifier.withDefaultNamespace("textures/block/ice.png")
			: original;
	}

	@ModifyReturnValue(
		method = "isShaking",
		at = @At("RETURN")
	)
	private boolean isShakingWhenFrozen(boolean original, @Local(argsOnly = true) LivingEntityRenderState state) {
		return original || state.sevenelements$isFrozen();
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getPose()Lnet/minecraft/world/entity/Pose;"
		)
	)
	private Pose forceFrozenPose(Pose original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForcePose, original);
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;solveBodyRot(Lnet/minecraft/world/entity/LivingEntity;FF)F"
		)
	)
	private float forceFrozenBodyYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceBodyYaw, original);
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;rotLerp(FFF)F"
		)
	)
	private float forceFrozenHeadYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceHeadYaw, original);
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;solveBodyRot(Lnet/minecraft/world/entity/LivingEntity;FF)F"
		)
	)
	private float forceFrozenPitch(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForcePitch, original);
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/WalkAnimationState;position(F)F"
		)
	)
	private float forceFrozenLimbDistance(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceLimbDistance, original);
	}

	@ModifyExpressionValue(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/WalkAnimationState;speed(F)F"
		)
	)
	private float forceFrozenLimbAngle(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceLimbAngle, original);
	}
}
