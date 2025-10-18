package io.github.xrickastley.sevenelements.renderer.genshin;

import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderLayer;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderPipelines;
import io.github.xrickastley.sevenelements.renderer.SevenElementsRenderer;
import io.github.xrickastley.sevenelements.util.Ease;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class ElementEntry {
	private static final float BLINK_SECONDS = 1.5f;
	private static final float BLINK_COUNT = 3;
	private static final BufferAllocator allocator = SevenElementsRenderer.createAllocator(RenderLayer.SOLID_BUFFER_SIZE);
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
		return entity.getEntityWorld().getTime() - this.appliedAt;
	}

	public void render(final LivingEntity entity, final MatrixStack matrixStack, final Camera camera, final float offset) {
		final float blinkInterval = ElementEntry.BLINK_SECONDS / ElementEntry.BLINK_COUNT;
		final float intervalSplit = blinkInterval / 2f;

		matrixStack.push();
		matrixStack.translate(0, entity.getBoundingBox().getLengthY() * 1.1, 0);
		matrixStack.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
		matrixStack.scale(0.50F, 0.50F, 0.50F);

		final float alpha = (float) (this.secondsLeft <= (BLINK_SECONDS + intervalSplit)
			? this.secondsLeft % blinkInterval <= intervalSplit
				? MathHelper.lerp((this.secondsLeft % blinkInterval) / intervalSplit, 0, 1)
				: MathHelper.lerp(((this.secondsLeft % blinkInterval) - 0.25) / intervalSplit, 1, 0)
			: 1);

		this.draw(matrixStack, camera, offset, alpha);

		if (this.getAppliedTicks(entity) <= 5) {
			final double animationProgress = Ease.LINEAR.applyLerpProgress(this.getAppliedTicks(entity) + tickDelta, 1, 6);
			final float scale2 = (float) (animationProgress * 2);
			final float alpha2 = (float) (1 - (animationProgress * 0.5));

			matrixStack.scale(scale2, scale2, scale2);

			this.draw(matrixStack, camera, offset, alpha2);
		}

		matrixStack.pop();
	}

	private void draw(final MatrixStack matrixStack, final Camera camera, final float offset, final float alpha) {
		final Identifier texture = this.element.getTexture();

		if (texture == null) return;

		final BufferBuilder buffer = SevenElementsRenderer.createBuffer(allocator, SevenElementsRenderPipelines.ELEMENTS);

		final float finalXOffset = -0.5f + offset;
		final Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

		buffer.vertex(positionMatrix, 0 + finalXOffset, 0, 0).texture(0f, 1f).color(1f, 1f, 1f, alpha);
		buffer.vertex(positionMatrix, 1 + finalXOffset, 0, 0).texture(1f, 1f).color(1f, 1f, 1f, alpha);
		buffer.vertex(positionMatrix, 1 + finalXOffset, 1, 0).texture(1f, 0f).color(1f, 1f, 1f, alpha);
		buffer.vertex(positionMatrix, 0 + finalXOffset, 1, 0).texture(0f, 0f).color(1f, 1f, 1f, alpha);

		SevenElementsRenderLayer.getElements(texture).draw(buffer.end());
	}
}
