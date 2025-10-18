package io.github.xrickastley.sevenelements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.BufferAllocator;
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

	@Unique
	private static final BufferAllocator sevenelements$quadAllocator = SevenElementsRenderer.createAllocator(SevenElementsRenderLayer::getQuads);
	@Unique
	private static final BufferAllocator sevenelements$linesAllocator = SevenElementsRenderer.createAllocator(RenderLayer.SOLID_BUFFER_SIZE);

	@Inject(
		method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
		at = @At("TAIL")
	)
	private void addRenderers(S state, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (!(state.sevenelements$getEntity() instanceof final LivingEntity entity)) return;

		final float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);

		this.sevenelements$renderElementsIfPresent(entity, matrixStack, tickDelta);
		this.sevenelements$renderElementalGauges(entity, matrixStack, tickDelta);
		this.sevenelements$renderCrystallizeShield(entity, matrixStack);
	}

	@Unique
	private void sevenelements$renderElementsIfPresent(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
		if (entity.isDead()) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final List<ElementEntry> elementArray = new ArrayList<>();

		if (component.hasValidLastReaction()) {
			final ElementalReaction reaction = component.getLastReaction().getLeft();
			final long reactionAt = component.getLastReaction().getRight();

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

		final Iterator<Vec3d> coords = this
			.sevenelements$generateTexturesUsingCenter(new Vec3d(0, 0, 0), 1, elementArray.size())
			.iterator();

		final Set<Identifier> elementTexs = new HashSet<>();

		elementArray.removeIf(entry -> !elementTexs.add(entry.getElement().getTexture()));
		elementArray.forEach(entry -> entry.render(entity, matrixStack, dispatcher.camera, (float) coords.next().getZ()));
	}

	@Unique
	private ArrayList<Vec3d> sevenelements$generateTexturesUsingCenter(Vec3d center, double length, int amount) {
		double totalDistance = length * (amount - 1);
		double offset = totalDistance / 2;

		final ArrayList<Vec3d> result = new ArrayList<>();
		double curDistance = center.getZ() + offset;
		for (int i = 0; i < amount; i++) {
			result.add(new Vec3d(center.getX(), center.getY(), curDistance));

			curDistance -= length;
		}

		return result;
	}

	@Unique
	private void sevenelements$renderElementalGauges(final LivingEntity entity, final MatrixStack matrixStack, final float tickDelta) {
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
	private void sevenelements$renderElementalGauge(final LivingEntity entity, final ElementalApplication application, final float yOffset, final MatrixStack matrixStack, final float tickDelta) {
		if (application.isEmpty()) return;

		final float GAUGE_SCALE = 0.35f;
		final float SCALE_PER_GU = 2.5f;

		final ClientConfig config = ClientConfig.get();

		matrixStack.push();
		matrixStack.translate(0f, entity.getBoundingBox().getLengthY() * 1.15, 0f);
		matrixStack.multiplyPositionMatrix(new Matrix4f().rotation(dispatcher.camera.getRotation()));
		matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE * 0.5f, GAUGE_SCALE);

		final float xOffset = (float) (entity.getBoundingBox().getLengthX() * 1.5f) / GAUGE_SCALE;
		final float gaugeWidth = application.isGaugeUnits()
			? (float) Math.min(SCALE_PER_GU * application.getGaugeUnits(), SCALE_PER_GU * 4)
			: 2 * SCALE_PER_GU;

		final Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
		final MatrixStack.Entry entry = matrixStack.peek();

		BufferBuilder buffer = SevenElementsRenderer.createBuffer(sevenelements$quadAllocator, SevenElementsRenderPipelines.QUADS);
		buffer.vertex(positionMatrix, 0 + xOffset, 0 - yOffset, 0).color(0xffffffff);
		buffer.vertex(positionMatrix, gaugeWidth + xOffset, 0 - yOffset, 0).color(0xffffffff);
		buffer.vertex(positionMatrix, gaugeWidth + xOffset, 1 - yOffset, 0).color(0xffffffff);
		buffer.vertex(positionMatrix, 0 + xOffset, 1 - yOffset, 0).color(0xffffffff);

		final float progress = this.sevenelements$getProgress(application, tickDelta);
		final Color elementColor = application.getElement().getDamageColor();
		final int color = application.isGaugeUnits()
			? elementColor.asARGB()
			: elementColor.multiply(1, 1, 1, 0.5).asARGB();

		buffer.vertex(positionMatrix, xOffset, 0 - yOffset, 0.0001f).color(color);
		buffer.vertex(positionMatrix, (gaugeWidth * progress) + xOffset, 0 - yOffset, 0.0001f).color(color);
		buffer.vertex(positionMatrix, (gaugeWidth * progress) + xOffset, 1 - yOffset, 0.0001f).color(color);
		buffer.vertex(positionMatrix, xOffset, 1 - yOffset, 0.0001f).color(color);

		if (application.isDuration()) {
			final float gaugeProgress = (float) (application.getCurrentGauge() / application.getGaugeUnits());

			buffer.vertex(positionMatrix, xOffset, 0 - yOffset, 0.0001f).color(color);
			buffer.vertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 0 - yOffset, 0.0001f).color(color);
			buffer.vertex(positionMatrix, (gaugeWidth * gaugeProgress) + xOffset, 1 - yOffset, 0.0001f).color(color);
			buffer.vertex(positionMatrix, xOffset, 1 - yOffset, 0.0001f).color(color);
		}

		SevenElementsRenderLayer.getQuads().draw(buffer.end());

		final float scaledGauge = (float) (0.1 * gaugeWidth / application.getGaugeUnits());
		final int splits = (int) Math.floor(gaugeWidth / (0.1 * gaugeWidth / application.getGaugeUnits()));

		for (int c = 1; c < splits && config.developer.displayGaugeRuler; c++) {
			final float i = c * scaledGauge;

			final float addedY = c % 10 == 0
				? 1f
				: c % 5 == 0
					? 0.5f
					: 0.25f;

			final RenderLayer layer = c % 10 == 0
				? SevenElementsRenderLayer.getThickLines()
				: SevenElementsRenderLayer.getThinLines();

			final Vec3d start = new Vec3d(xOffset + i, 0 - yOffset, -0.0005f);
			final Vec3d end = new Vec3d(xOffset + i, addedY - yOffset, -0.0005f);
			final Vec3d normal = end.normalize();

			buffer = SevenElementsRenderer.createBuffer(sevenelements$quadAllocator, SevenElementsRenderPipelines.LINES);
			buffer
				.vertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
				.color(0xff000000)
				.normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
			buffer
				.vertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
				.color(0xff000000)
				.normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);

			layer.draw(buffer.end());
		}

		matrixStack.pop();
	}

	@Unique
	private float sevenelements$getProgress(ElementalApplication application, float tickDelta) {
		return application instanceof final DurationElementalApplication durationApp
			? (float) ((application.getRemainingTicks() - tickDelta) / durationApp.getDuration())
			: (float) (application.getCurrentGauge() / application.getGaugeUnits());
	}

	@Unique
	private void sevenelements$renderCrystallizeShield(final LivingEntity entity, final MatrixStack matrixStack) {
		final ClientConfig config = ClientConfig.get();

		if (!SpecialEffectsRenderer.shouldRender(entity)) return;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final @Nullable Pair<Element, Double> crystallizeShield = component.getCrystallizeShield();

		if (crystallizeShield == null) return;

		final double lengthY = entity.getBoundingBox().getLengthY();

		matrixStack.push();
		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(dispatcher.camera.getYaw()));
		matrixStack.translate(0, lengthY * 0.6, 0);

		SphereRenderer.render(
			matrixStack,
			new Vec3d(0, 0, 0),
			(float) (lengthY / 2 * 1.25),
			config.rendering.elements.sphereResolution,
			config.rendering.elements.sphereResolution * 2,
			pos -> crystallizeShield.getLeft().getDamageColor().multiply(1, 1, 1, 0.75 * Math.pow(pos.x, 4)).asARGB()
		);

		matrixStack.pop();
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
		method = "getRenderLayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)Lnet/minecraft/util/Identifier;"
		)
	)
	private Identifier renderFrostedModel(Identifier original, @Local(argsOnly = true) LivingEntityRenderState state) {
		if (!ClientConfig.getEffectRenderType().allowsSpecialEffects()) return original;

		return state.sevenelements$getEntity() instanceof final LivingEntity entity
			? this.sevenelements$ifFrozen(entity, c -> Identifier.of("minecraft", "textures/block/ice.png"), original)
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
		method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
		at = @At("HEAD")
	)
	private void forceFrozenPose(S state, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
		if (!(state.sevenelements$getEntity() instanceof final LivingEntity entity)) return;

		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		if (component.isFrozen()) entity.setPose(component.getForcePose());
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
