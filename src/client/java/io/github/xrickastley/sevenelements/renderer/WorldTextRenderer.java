package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import io.github.xrickastley.sevenelements.renderer.state.WorldTextState;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.Color;
import io.github.xrickastley.sevenelements.util.Ease;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class WorldTextRenderer extends SevenElementsRenderer<WorldTextState> {
	private final List<TextEntry> entries = new ArrayList<>();

	public WorldTextRenderer() {
		super(SevenElementsRenderer.CLEAR_RENDER_STATES);

		this.registerRenderStage(SevenElementsRenderPipelines.WORLD_TEXT, 0, RenderType.BIG_BUFFER_SIZE, this::render);
	}

	@Override
	protected void extract(LevelExtractionContext context) {
		super.extract(context);

		this.entries
			.stream()
			.map(Functions.withArgument(TextEntry::extract, context))
			.forEachOrdered(this::addRenderState);
	}

	private void render(LevelRenderContext context, StagedVertexBuffer stagedBuffer, WorldTextState state) {
		final Minecraft client = Minecraft.getInstance();
		final Font textRenderer = client.font;
		final ClientConfig config = ClientConfig.get();

		final Camera camera = context.gameRenderer().mainCamera();
		final PoseStack matrices = context.poseStack();

		final double d = camera.position().x;
		final double e = camera.position().y;
		final double f = camera.position().z;

		final float scale = (float) (state.size() * config.rendering.text.globalTextScale);

		matrices.pushPose();
		matrices.translate((float) (state.x() - d), (float) (state.y() - e), (float) (state.z() - f));
		matrices.mulPose(new Matrix4f().rotation(camera.rotation()));
		matrices.scale(scale, -scale, scale);

		float g = state.center() ? (-textRenderer.width(state.text()) / 2.0f) : 0.0f;
		g -= state.offset() / state.size();

		final Font.PreparedText text = textRenderer.prepareText(state.text(), g, 0.0f, state.color(), false, true, 0);

		final Matrix4f pose = matrices.last().pose();

		text.visit(new GlyphRenderer(stagedBuffer, state.visibleThroughObjects() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, pose));

		matrices.popPose();
	}

	@Override
	protected void tick(ClientLevel world) {
		super.tick(world);

		final Iterator<TextEntry> entryIterator = this.entries.iterator();

		while (entryIterator.hasNext()) {
			final TextEntry entry = entryIterator.next();

			entry.tick();

			if (entry.shouldRemove())
				entryIterator.remove();
		}
	}

	public WorldTextRenderer addEntry(TextEntry entry) {
		this.entries.add(entry);

		return this;
	}

	private final class GlyphRenderer implements Font.GlyphVisitor {
		private StagedVertexBuffer stagedBuffer;
		private Font.DisplayMode displayMode;
		private Matrix4f pose;
		private boolean force;

		GlyphRenderer(StagedVertexBuffer stagedBuffer, Font.DisplayMode displayMode, Matrix4f pose) {
			this.stagedBuffer = stagedBuffer;
			this.displayMode = displayMode;
			this.pose = pose;
			this.force = true;
		}

		@Override
		public void acceptRenderable(TextRenderable renderable) {
			final VertexConsumer consumer = WorldTextRenderer.this.getVertexBuilder(this.stagedBuffer, renderable.renderType(this.displayMode), force);

			this.force = false;

			renderable.render(this.pose, consumer, 15728880, false);
		}
	}

	public static abstract class TextEntry {
		protected final double x;
		protected final double y;
		protected final double z;
		protected final Color color;
		protected int age;

		protected TextEntry(double x, double y, double z, Color color) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.color = color;
			this.age = 0;
		}

		protected abstract WorldTextState extract(LevelExtractionContext context);

		protected void tick() {
			this.age++;
		}

		protected abstract boolean shouldRemove();
	}

	public static final class ReactionText extends TextEntry {
		protected final Component text;
		protected final int maxAge = 30;
		protected final int fadeAge = maxAge - 15;
		protected final int scaleAge = 8;

		public ReactionText(double x, double y, double z, Color color, Component text) {
			super(x, y, z, color);

			this.text = text;
		}

		@Override
		protected WorldTextState extract(LevelExtractionContext context) {
			final float deltaTime = age + context.deltaTracker().getGameTimeDeltaPartialTick(false);
			final double alpha = Math.max(0.0f, Mth.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = 1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5);

			return new WorldTextState(
				this.text.getVisualOrderText(),
				this.x,
				this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f,
				this.z,
				this.color.multiply(1, 1, 1, alpha).asARGB(),
				(float) (0.04 * scale),
				true,
				0f,
				true
			);
		}

		@Override
		protected boolean shouldRemove() {
			return age > maxAge;
		}
	}

	public static final class DamageText extends TextEntry {
		protected final int maxAge = 30;
		protected final int fadeAge = maxAge - 15;
		protected final int scaleAge = 12;
		protected final Component amount;
		protected final double scale;

		public DamageText(double x, double y, double z, Color color, double amount, double scale) {
			super(x, y, z, color);

			final ClientConfig config = ClientConfig.get();
			final String damageFormat = config.developer.commafyDamage
				? "%,.0f"
				: "%.0f";

			this.amount = TextHelper.font(String.format(damageFormat, Math.max(amount, 1)), TextHelper.GENSHIN_FONT);
			this.scale = scale;
		}

		@Override
		protected WorldTextState extract(LevelExtractionContext context) {
			final float deltaTime = age + context.deltaTracker().getGameTimeDeltaPartialTick(false);
			final double alpha = Math.max(0.0f, Mth.lerp((deltaTime - fadeAge) / (maxAge - fadeAge), 1.0, 0.0));
			final double scale = (1.25 - (Ease.IN_OUT_QUART.applyLerpProgress(deltaTime / scaleAge, 0, 1) * 0.5)) * this.scale;

			return new WorldTextState(
				this.amount.getVisualOrderText(),
				this.x,
				this.y + Ease.OUT_SINE.applyLerpProgress(deltaTime, 0, maxAge) * 0.75f,
				this.z,
				this.color.multiply(1, 1, 1, alpha).asARGB(),
				(float) (0.04 * scale),
				true,
				0f,
				true
			);
		}

		@Override
		protected boolean shouldRemove() {
			return age > maxAge;
		}
	}
}
