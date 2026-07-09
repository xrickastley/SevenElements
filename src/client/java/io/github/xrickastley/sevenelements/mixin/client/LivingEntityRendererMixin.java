package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.genshin.ElementRenderer;
import io.github.xrickastley.sevenelements.renderer.genshin.SpecialEffectsRenderer;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.SphereRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {
	protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
		super(context);

		throw new AssertionError();
	}

	@Inject(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void addSevenElementsLivingEntityRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
		livingEntityRenderState.sevenelements$fillRenderState(livingEntity, f);
	}

	@Inject(
		method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at = @At("TAIL")
	)
	private void addRenderers(S state, MatrixStack matrixStack, VertexConsumerProvider provider, int i, CallbackInfo ci) {
		if (state.sevenelements$isDead()) return;

		this.sevenelements$renderElements(state, provider, matrixStack);
		this.sevenelements$renderElementalGauges(state, provider, matrixStack);
		this.sevenelements$renderCrystallizeShield(state, provider, matrixStack);
	}

	@Unique
	private void sevenelements$renderElements(final S entityState, final VertexConsumerProvider provider, final MatrixStack matrixStack) {
		final List<ElementRenderer.ElementState> elementStates = entityState.sevenelements$getElementStates();

		if (elementStates.isEmpty()) return;
		
		final int length = 1;
		final int amount = elementStates.size();

		final double totalDistance = length * (amount - 1);
		final double offset = totalDistance / 2;

		final ArrayList<Vec3d> result = new ArrayList<>();
		double curDistance = offset;
		for (int i = 0; i < amount; i++) {
			result.add(new Vec3d(0, 0, curDistance));

			curDistance -= length;
		}

		final Iterator<Vec3d> coords = result.iterator();

		elementStates
			.stream()
			.map(state -> new Pair<>(state, (float) coords.next().getZ()))
			.forEachOrdered(statePair -> ElementRenderer.renderElement(entityState, statePair.getLeft(), provider, matrixStack, dispatcher.camera, statePair.getRight()));
	}

	@Unique
	private void sevenelements$renderElementalGauges(final S entityState, final VertexConsumerProvider provider, final MatrixStack matrixStack) {
		final ClientConfig config = ClientConfig.get();
		final List<ElementRenderer.ElementGaugeState> gaugeStates = entityState.sevenelements$getGaugeStates();

		if (!config.developer.displayElementalGauges || gaugeStates.isEmpty()) return;

		final int elementCount = gaugeStates.size();
		final Iterator<ElementRenderer.ElementGaugeState> stateIterator = gaugeStates.iterator();

		Stream
			.iterate(0.0f, n -> (n / 1.25f) < elementCount, n -> n + 1.25f)
			.map(yOffset -> new Pair<>(stateIterator.next(), yOffset))
			.forEachOrdered(statePair -> ElementRenderer.renderElementalGauge(entityState, statePair.getLeft(), provider, matrixStack, dispatcher.camera, statePair.getRight() - 0.5f));
	}

	@Unique
	private void sevenelements$renderCrystallizeShield(final S state, final VertexConsumerProvider provider, final MatrixStack matrixStack) {
		final ClientConfig config = ClientConfig.get();

		if (!SpecialEffectsRenderer.shouldRender(state) || state.sevenelements$getCrystallizeShieldElement() == null) return;

		final double lengthY = state.sevenelements$getBoundingBoxLength().getY();

		matrixStack.push();
		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(dispatcher.camera.getYaw()));
		matrixStack.translate(0, lengthY * 0.6, 0);

		SphereRenderer.render(
			provider.getBuffer(SevenElementsRenderLayer.getCrystallizeShield()),
			matrixStack,
			new Vec3d(0, 0, 0),
			(float) (lengthY / 2 * 1.25),
			config.rendering.elements.sphereResolution,
			config.rendering.elements.sphereResolution * 2,
			pos -> state.sevenelements$getCrystallizeShieldElement().getDamageColor().multiply(1, 1, 1, 0.75 * Math.pow(pos.x, 4)).asARGB()
		);

		matrixStack.pop();
	}

	@Unique
	private <R> R sevenelements$ifFrozen(LivingEntity entity, Function<FrozenEffectComponent, R> ifFrozen, R ifNotFrozen) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		return component.isFrozen()
			? ifFrozen.apply(component)
			: ifNotFrozen;
	}

	@ModifyExpressionValue(
		method = "getRenderLayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)Lnet/minecraft/util/Identifier;"
		)
	)
	private Identifier renderFrostedModel(Identifier original, @Local(argsOnly = true) LivingEntityRenderState state) {
		return ClientConfig.getEffectRenderType().allowsSpecialEffects() && state.sevenelements$isFrozen()
			? Identifier.ofVanilla("textures/block/ice.png")
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
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;getPose()Lnet/minecraft/entity/EntityPose;"
		)
	)
	private EntityPose forceFrozenPose(EntityPose original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForcePose, original);
	}

	@ModifyExpressionValue(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;clampBodyYaw(Lnet/minecraft/entity/LivingEntity;FF)F"
		)
	)
	private float forceFrozenBodyYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceBodyYaw, original);
	}

	@ModifyExpressionValue(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F"
		)
	)
	private float forceFrozenHeadYaw(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceHeadYaw, original);
	}

	@ModifyExpressionValue(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;clampBodyYaw(Lnet/minecraft/entity/LivingEntity;FF)F"
		)
	)
	private float forceFrozenPitch(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForcePitch, original);
	}

	@ModifyExpressionValue(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getAnimationProgress(F)F"
		)
	)
	private float forceFrozenLimbDistance(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceLimbDistance, original);
	}

	@ModifyExpressionValue(
		method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LimbAnimator;getAmplitude(F)F"
		)
	)
	private float forceFrozenLimbAngle(float original, @Local(argsOnly = true) LivingEntity entity) {
		return this.sevenelements$ifFrozen(entity, FrozenEffectComponent::getForceLimbAngle, original);
	}
}
