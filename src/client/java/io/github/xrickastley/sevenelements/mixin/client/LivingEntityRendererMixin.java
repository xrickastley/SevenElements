package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.element.DurationElementalApplication;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.renderer.genshin.ElementEntry;
import io.github.xrickastley.sevenelements.renderer.genshin.SpecialEffectsRenderer;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.SphereRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {
	protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
		super(context);

		throw new AssertionError();
	}

	@Unique
	private static final ByteBufferBuilder sevenelements$quadAllocator = SevenElementsRenderer.createAllocator(SevenElementsRenderLayer::getQuads);

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("TAIL")
	)
	private void addRenderers(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		if (!(state.sevenelements$getEntity() instanceof final LivingEntity entity)) return;

		final float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

		this.sevenelements$renderElementsIfPresent(entity, poseStack, tickDelta);
		this.sevenelements$renderElementalGauges(entity, poseStack, tickDelta);
		this.sevenelements$renderCrystallizeShield(entity, poseStack);
	}

	@Unique
	private void sevenelements$renderElementsIfPresent(final LivingEntity entity, final PoseStack matrixStack, final float tickDelta) {
		if (entity.isDeadOrDying()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final List<ElementEntry> elementArray = new ArrayList<>();

		if (component.hasValidLastReaction()) {
			final ElementalReaction reaction = component.getLastReaction().getA();
			final long reactionAt = component.getLastReaction().getB();

			reaction
				.getReactionDisplayOrder()
				.forEach(element -> elementArray.add(new ElementEntry(element, 60.0, reactionAt, tickDelta)));
		} else {
			if (component.getAppliedElements().isEmpty()) return;

			final Optional<Integer> priority = component.getHighestElementPriority();

			if (priority.isEmpty()) return;

			elementArray.addAll(
				component
					.getAppliedElements()
					.filter(application -> application.getElement().getPriority() == priority.get())
					.map(a -> ElementEntry.of(a, tickDelta))
			);
		}

		final Set<Identifier> textures = new HashSet<>();

		elementArray.removeIf(entry -> !entry.getElement().hasTexture() || !textures.add(entry.getElement().getTexture()));

		final Iterator<Vec3> coords = this
			.sevenelements$generateTexturesUsingCenter(new Vec3(0, 0, 0), 1, elementArray.size())
			.iterator();

		final Set<Identifier> elementTexs = new HashSet<>();

		elementArray.removeIf(entry -> !elementTexs.add(entry.getElement().getTexture()));
		elementArray.forEach(entry -> entry.render(entity, matrixStack, entityRenderDispatcher.camera, (float) coords.next().z()));
	}

	@Unique
	private ArrayList<Vec3> sevenelements$generateTexturesUsingCenter(Vec3 center, double length, int amount) {
		double totalDistance = length * (amount - 1);
		double offset = totalDistance / 2;

		final ArrayList<Vec3> result = new ArrayList<>();
		double curDistance = center.z() + offset;
		for (int i = 0; i < amount; i++) {
			result.add(new Vec3(center.x(), center.y(), curDistance));

			curDistance -= length;
		}

		return result;
	}

	@Unique
	private void sevenelements$renderElementalGauges(final LivingEntity entity, final PoseStack matrixStack, final float tickDelta) {
		final ClientConfig config = ClientConfig.get();

		if (!config.developer.displayElementalGauges) return;

		if (!entity.isAlive()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ArrayList<ElementalApplication> appliedElements = new ArrayList<>(component
			.getAppliedElements()
			.sortElements((a, b) -> a.getElement().getPriority() - b.getElement().getPriority()));

		final int elementCount = appliedElements.size();
		final Iterator<ElementalApplication> aeIterator = appliedElements.iterator();

		Stream
			.iterate(0.0f, n -> (n / 1.25f) < elementCount, n -> n + 1.25f)
			.forEachOrdered(yOffset ->
				sevenelements$renderElementalGauge(entity, aeIterator.next(), yOffset - 0.5f, matrixStack, tickDelta)
			);
	}

	@Unique
	private void sevenelements$renderElementalGauge(final LivingEntity entity, final ElementalApplication application, final float yOffset, final PoseStack matrixStack, final float tickDelta) {
		if (application.isEmpty()) return;

		final float GAUGE_SCALE = 0.35f;
		final float SCALE_PER_GU = 2.5f;

		final ClientConfig config = ClientConfig.get();

		matrixStack.pushPose();
		matrixStack.translate(0f, entity.getBoundingBox().getYsize() * 1.15, 0f);
		matrixStack.mulPose(new Matrix4f().rotation(entityRenderDispatcher.camera.rotation()));
		matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE * 0.5f, GAUGE_SCALE);

		final float xOffset = (float) (entity.getBoundingBox().getXsize() * 1.5f) / GAUGE_SCALE;
		final float gaugeWidth = application.isGaugeUnits()
			? (float) Math.min(SCALE_PER_GU * application.getGaugeUnits(), SCALE_PER_GU * 4)
			: 2 * SCALE_PER_GU;

		final Matrix4f positionMatrix = matrixStack.last().pose();
		final PoseStack.Pose entry = matrixStack.last();

		BufferBuilder buffer = SevenElementsRenderer.createBuffer(sevenelements$quadAllocator, SevenElementsRenderPipelines.QUADS);
		buffer.addVertex(positionMatrix, 0 + xOffset, 0 - yOffset, 0).setColor(0xffffffff);
		buffer.addVertex(positionMatrix, gaugeWidth + xOffset, 0 - yOffset, 0).setColor(0xffffffff);
		buffer.addVertex(positionMatrix, gaugeWidth + xOffset, 1 - yOffset, 0).setColor(0xffffffff);
		buffer.addVertex(positionMatrix, 0 + xOffset, 1 - yOffset, 0).setColor(0xffffffff);

		final float progress = this.sevenelements$getProgress(application, tickDelta);
		final Color elementColor = application.getElement().getDamageColor();
		final int color = application.isGaugeUnits()
			? elementColor.asARGB()
			: elementColor.multiply(1, 1, 1, 0.5).asARGB();

		buffer.addVertex(positionMatrix, xOffset, 0 - yOffset, 0.0001f).setColor(color);
		buffer.addVertex(positionMatrix, (gaugeWidth * progress) + xOffset, 0 - yOffset, 0.0001f).setColor(color);
		buffer.addVertex(positionMatrix, (gaugeWidth * progress) + xOffset, 1 - yOffset, 0.0001f).setColor(color);
		buffer.addVertex(positionMatrix, xOffset, 1 - yOffset, 0.0001f).setColor(color);

		if (application.isDuration()) {
			final float gaugeProgress = (float) (application.getCurrentGauge() / application.getGaugeUnits());

			buffer.addVertex(positionMatrix, xOffset, 0 - yOffset, 0.0001f).setColor(color);
			buffer.addVertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 0 - yOffset, 0.0001f).setColor(color);
			buffer.addVertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 1 - yOffset, 0.0001f).setColor(color);
			buffer.addVertex(positionMatrix, xOffset, 1 - yOffset, 0.0001f).setColor(color);
		}

		SevenElementsRenderLayer.getQuads().draw(buffer.buildOrThrow());

		final float scaledGauge = (float) (0.1 * gaugeWidth / application.getGaugeUnits());
		final int splits = (int) Math.floor(gaugeWidth / (0.1 * gaugeWidth / application.getGaugeUnits()));

		for (int c = 1; c < splits && config.developer.displayGaugeRuler; c++) {
			final float i = c * scaledGauge;

			final float addedY = c % 10 == 0
				? 1f
				: c % 5 == 0
					? 0.5f
					: 0.25f;

			final int lineWidth = c % 10 == 0
				? 10
				: 5;

			final Vec3 start = new Vec3(xOffset + i, 0 - yOffset, -0.0005f);
			final Vec3 end = new Vec3(xOffset + i, addedY - yOffset, -0.0005f);
			final Vec3 normal = end.normalize();

			buffer = SevenElementsRenderer.createBuffer(sevenelements$quadAllocator, SevenElementsRenderPipelines.LINES);
			buffer
				.addVertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
				.setColor(0xff000000)
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(lineWidth);
			buffer
				.addVertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
				.setColor(0xff000000)
				.setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
				.setLineWidth(lineWidth);

			SevenElementsRenderLayer.getChargeLine().draw(buffer.buildOrThrow());
		}

		matrixStack.popPose();
	}

	@Unique
	private float sevenelements$getProgress(ElementalApplication application, float tickDelta) {
		return application instanceof final DurationElementalApplication durationApp
			? (float) ((application.getRemainingTicks() - tickDelta) / durationApp.getDuration())
			: (float) (application.getCurrentGauge() / application.getGaugeUnits());
	}

	@Unique
	private void sevenelements$renderCrystallizeShield(final LivingEntity entity, final PoseStack matrixStack) {
		final ClientConfig config = ClientConfig.get();

		if (!SpecialEffectsRenderer.shouldRender(entity)) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final @Nullable Tuple<Element, Double> crystallizeShield = component.getCrystallizeShield();

		if (crystallizeShield == null) return;

		final double lengthY = entity.getBoundingBox().getYsize();

		matrixStack.pushPose();
		matrixStack.mulPose(Axis.YN.rotationDegrees(entityRenderDispatcher.camera.yRot()));
		matrixStack.translate(0, lengthY * 0.6, 0);

		SphereRenderer.render(
			matrixStack,
			new Vec3(0, 0, 0),
			(float) (lengthY / 2 * 1.25),
			config.rendering.elements.sphereResolution,
			config.rendering.elements.sphereResolution * 2,
			pos -> crystallizeShield.getA().getDamageColor().multiply(1, 1, 1, 0.75 * Math.pow(pos.x, 4)).asARGB()
		);

		matrixStack.popPose();
	}

	@Unique
	private FrozenEffectComponent sevenelements$getComponent(LivingEntity entity) {
		return FrozenEffectComponent.KEY.get(entity);
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
		if (!ClientConfig.getEffectRenderType().allowsSpecialEffects()) return original;

		return state.sevenelements$getEntity() instanceof final LivingEntity entity
			? this.sevenelements$ifFrozen(entity, c -> Identifier.fromNamespaceAndPath("minecraft", "textures/block/ice.png"), original)
			: original;
	}

	@ModifyReturnValue(
		method = "isShaking",
		at = @At("RETURN")
	)
	private boolean isShakingWhenFrozen(boolean original, @Local(argsOnly = true) LivingEntityRenderState state) {
		return original
			|| (state.sevenelements$getEntity() instanceof final LivingEntity entity && this.sevenelements$getComponent(entity).isFrozen());
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD")
	)
	private void forceFrozenPose(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		if (!(state.sevenelements$getEntity() instanceof final LivingEntity entity)) return;

		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		if (component.isFrozen()) entity.setPose(component.getForcePose());
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
