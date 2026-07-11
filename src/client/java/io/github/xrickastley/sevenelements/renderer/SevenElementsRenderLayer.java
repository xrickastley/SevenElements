package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.OptionalDouble;
import java.util.SequencedMap;
import java.util.function.Function;

import org.joml.Matrix4f;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer.OutlineMode;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.LineWidth;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class SevenElementsRenderLayer {
	public static final RenderPhase.Texturing STATIC_GLINT_TEXTURING = new RenderPhase.Texturing(
		"seven-elements:static_glint_texturing", () -> setupStaticGlintTexturing(8.0F), () -> RenderSystem.resetTextureMatrix()
	);

	public static final RenderPhase.Texturing STATIC_ENTITY_GLINT_TEXTURING = new RenderPhase.Texturing(
		"seven-elements:static_glint_texturing", () -> setupStaticGlintTexturing(0.16F), () -> RenderSystem.resetTextureMatrix()
	);

	private static final RenderLayer TRIANGLES = RenderLayer.of(
		"seven-elements:triangles",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLES,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer GAUGE_DISPLAY = RenderLayer.of(
		"seven-elements:gauge_display",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.QUADS,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer THIN_LINES = RenderLayer.of(
		"seven-elements:thin_lines",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.LINES,
		RenderLayer.MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(5)))
			.build(OutlineMode.NONE)
	);

	private static final RenderLayer THICK_LINES = RenderLayer.of(
		"seven-elements:thick_lines",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.LINES,
		RenderLayer.MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(10)))
			.build(OutlineMode.NONE)
	);

	private static final Function<Identifier, RenderLayer> ELEMENTS = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:elements",
			RenderLayer.SOLID_BUFFER_SIZE,
			SevenElementsRenderPipelines.ELEMENTS,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.build(OutlineMode.NONE)
		)
	);

	private static final RenderLayer WORLD_TEXT = RenderLayer.of(
		"seven-elements:world/text",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.WORLD_TEXT,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer SPHERE = RenderLayer.of(
		"seven-elements:sphere",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.SPHERE,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer CRYSTALLIZE_SHIELD = RenderLayer.of(
		"seven-elements:crystallize_shield",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.SPHERE,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final Function<Identifier, RenderLayer> ARMOR_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:armor_entity_element_glint",
			1536,
			RenderPipelines.GLINT,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.texturing(RenderPhase.ARMOR_ENTITY_GLINT_TEXTURING)
				.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
				.build(false)
		)
	);

	public static final Function<Identifier, RenderLayer> ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_glint",
			1536,
			RenderPipelines.GLINT,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.texturing(RenderPhase.GLINT_TEXTURING)
				.build(false)
		)
	);

	public static final Function<Identifier, RenderLayer> STATIC_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:static_element_glint",
			1536,
			RenderPipelines.GLINT,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.texturing(SevenElementsRenderLayer.STATIC_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			1536,
			RenderPipelines.GLINT,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> STATIC_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			1536,
			RenderPipelines.GLINT,
			RenderLayer.MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, false))
				.texturing(SevenElementsRenderLayer.STATIC_ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final SequencedMap<RenderLayer, BufferAllocator> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayer.WORLD_TEXT, SevenElementsRenderer.createAllocator(RenderLayer.SOLID_BUFFER_SIZE));
		}
	);

	private static final VertexConsumerProvider.Immediate WORLD_TEXT_IMMEDIATE = VertexConsumerProvider.immediate(WORLD_TEXT_SEQUENCED_MAP, SevenElementsRenderer.createAllocator(RenderLayer.DEFAULT_BUFFER_SIZE));

	public static RenderLayer getTriangles() {
		return SevenElementsRenderLayer.TRIANGLES;
	}

	public static RenderLayer getGaugeDisplay() {
		return SevenElementsRenderLayer.GAUGE_DISPLAY;
	}

	public static RenderLayer getThinLines() {
		return SevenElementsRenderLayer.THIN_LINES;
	}

	public static RenderLayer getThickLines() {
		return SevenElementsRenderLayer.THICK_LINES;
	}

	public static Function<Identifier, RenderLayer> getElements() {
		return SevenElementsRenderLayer.ELEMENTS;
	}

	public static RenderLayer getElements(Identifier texture) {
		return SevenElementsRenderLayer.ELEMENTS.apply(texture);
	}

	public static RenderLayer getWorldText() {
		return SevenElementsRenderLayer.WORLD_TEXT;
	}

	public static RenderLayer getSphere() {
		return SevenElementsRenderLayer.SPHERE;
	}

	public static RenderLayer getCrystallizeShield() {
		return SevenElementsRenderLayer.CRYSTALLIZE_SHIELD;
	}

	public static VertexConsumerProvider.Immediate getWorldTextImmediate() {
		return WORLD_TEXT_IMMEDIATE;
	}

	public static Function<Identifier, RenderLayer> getArmorEntityElementGlint() {
		return SevenElementsRenderLayer.ARMOR_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getArmorEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.ARMOR_ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getElementGlint() {
		return SevenElementsRenderLayer.ELEMENT_GLINT;
	}

	public static RenderLayer getElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticElementGlint() {
		return SevenElementsRenderLayer.STATIC_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.STATIC_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getEntityElementGlint() {
		return SevenElementsRenderLayer.ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticEntityElementGlint() {
		return SevenElementsRenderLayer.STATIC_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.STATIC_ENTITY_ELEMENT_GLINT.apply(texture);
	}



	private static void setupStaticGlintTexturing(float scale) {
		Matrix4f matrix4f = new Matrix4f().translation(0.0F, 0.0F, 0.0F);
		matrix4f.rotateZ((float) (Math.PI / 18)).scale(scale);
		RenderSystem.setTextureMatrix(matrix4f);
	}
}
