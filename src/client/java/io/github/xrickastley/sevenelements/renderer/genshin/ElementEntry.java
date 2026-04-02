package io.github.xrickastley.sevenelements.renderer.genshin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.util.Ease;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ElementEntry {
	private static final float BLINK_SECONDS = 1.5f;
	private static final float BLINK_COUNT = 3;
	private static final ByteBufferBuilder allocator = SevenElementsRenderer.createAllocator(RenderType.BIG_BUFFER_SIZE);
	private final Element element;
	private final double secondsLeft;
	private final long appliedAt;
	private final float tickDelta;

	public ElementEntry(Element element, double secondsLeft, long appliedAt, float tickDelta) {
		this.element = element;
		this.secondsLeft = secondsLeft;
		this.appliedAt = appliedAt;
		this.tickDelta = tickDelta;
	}

	public static ElementEntry of(ElementalApplication application, float tickDelta) {
		return new ElementEntry(application.getElement(), (application.getRemainingTicks() - tickDelta) / 20.0, application.getAppliedAt(), tickDelta);
	}

	public Element getElement() {
		return element;
	}

	private long getAppliedTicks(final Entity entity) {
		return entity.level().getGameTime() - this.appliedAt;
	}

	public void render(final LivingEntity entity, final PoseStack matrixStack, final Camera camera, final float offset) {
		final float blinkInterval = ElementEntry.BLINK_SECONDS / ElementEntry.BLINK_COUNT;
		final float intervalSplit = blinkInterval / 2f;

		matrixStack.pushPose();
		matrixStack.translate(0, entity.getBoundingBox().getYsize() * 1.1, 0);
		matrixStack.mulPose(new Matrix4f().rotation(camera.rotation()));
		matrixStack.scale(0.50F, 0.50F, 0.50F);

		final float alpha = (float) (this.secondsLeft <= (BLINK_SECONDS + intervalSplit)
			? this.secondsLeft % blinkInterval <= intervalSplit
				? Mth.lerp((this.secondsLeft % blinkInterval) / intervalSplit, 0, 1)
				: Mth.lerp(((this.secondsLeft % blinkInterval) - 0.25) / intervalSplit, 1, 0)
			: 1);

		this.draw(matrixStack, camera, offset, alpha);

		if (this.getAppliedTicks(entity) <= 5) {
			final double animationProgress = Ease.LINEAR.applyLerpProgress(this.getAppliedTicks(entity) + tickDelta, 1, 6);
			final float scale2 = (float) (animationProgress * 2);
			final float alpha2 = (float) (1 - (animationProgress * 0.5));

			matrixStack.scale(scale2, scale2, scale2);

			this.draw(matrixStack, camera, offset, alpha2);
		}

		matrixStack.popPose();
	}

	private void draw(final PoseStack matrixStack, final Camera camera, final float offset, final float alpha) {
		final Identifier texture = this.element.getTexture();

		if (texture == null) return;

		final BufferBuilder buffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.ELEMENTS);

		final float finalXOffset = -0.5f + offset;
		final Matrix4f positionMatrix = matrixStack.last().pose();

		buffer.addVertex(positionMatrix, 0 + finalXOffset, 0, 0).setUv(0f, 1f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex(positionMatrix, 1 + finalXOffset, 0, 0).setUv(1f, 1f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex(positionMatrix, 1 + finalXOffset, 1, 0).setUv(1f, 0f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex(positionMatrix, 0 + finalXOffset, 1, 0).setUv(0f, 0f).setColor(1f, 1f, 1f, alpha);

		SevenElementsRenderLayer.getElements(texture).draw(buffer.buildOrThrow());
	}
}
