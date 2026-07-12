package io.github.xrickastley.sevenelements.renderer;

import java.util.SequencedMap;
import java.util.function.Function;

import org.joml.Matrix4f;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup.OutlineMode;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.TextureTransform;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class SevenElementsRenderLayers {
	public static final TextureTransform STATIC_GLINT_TEXTURING = new TextureTransform(
		"seven-elements:static_glint_texturing", () -> getStaticGlintTransformation(8.0F)
	);

	public static final TextureTransform STATIC_ENTITY_GLINT_TEXTURING = new TextureTransform(
		"seven-elements:static_glint_texturing", () -> getStaticGlintTransformation(0.16F)
	);

	private static final RenderLayer TRIANGLES = RenderLayer.of(
		"seven-elements:triangles",
		RenderSetup
			.builder(SevenElementsRenderPipelines.TRIANGLES)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final RenderLayer GAUGE_DISPLAY = RenderLayer.of(
		"seven-elements:gauge_display",
		RenderSetup
			.builder(SevenElementsRenderPipelines.QUADS)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final RenderLayer RULER_LINES = RenderLayer.of(
		"seven-elements:ruler_lines",
		RenderSetup
			.builder(SevenElementsRenderPipelines.LINES)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final Function<Identifier, RenderLayer> ELEMENTS = Util.memoize(
		texture -> {
			return RenderLayer.of(
				"seven-elements:elements",
				RenderSetup
					.builder(SevenElementsRenderPipelines.ELEMENTS)
					.outlineMode(OutlineMode.NONE)
					.texture("Sampler0", texture)
					.build()
			);
		}
	);

	private static final RenderLayer WORLD_TEXT = RenderLayer.of(
		"seven-elements:world/text",
		RenderSetup
			.builder(SevenElementsRenderPipelines.WORLD_TEXT)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final RenderLayer SPHERE = RenderLayer.of(
		"seven-elements:sphere",
		RenderSetup
			.builder(SevenElementsRenderPipelines.SPHERE)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final RenderLayer CRYSTALLIZE_SHIELD = RenderLayer.of(
		"seven-elements:crystallize_shield",
		RenderSetup
			.builder(SevenElementsRenderPipelines.SPHERE)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final Function<Identifier, RenderLayer> ARMOR_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:armor_entity_element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.outlineMode(OutlineMode.NONE)
				.texture("Sampler0", texture)
				.textureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
				.layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
				.build()
		)
	);

	public static final Function<Identifier, RenderLayer> ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.outlineMode(OutlineMode.NONE)
				.texture("Sampler0", texture)
				.textureTransform(TextureTransform.GLINT_TEXTURING)
				.build()
		)
	);

	public static final Function<Identifier, RenderLayer> STATIC_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:static_element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.outlineMode(OutlineMode.NONE)
				.texture("Sampler0", texture)
				.textureTransform(SevenElementsRenderLayers.STATIC_GLINT_TEXTURING)
				.build()
		)
	);

	private static final Function<Identifier, RenderLayer> ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.outlineMode(OutlineMode.NONE)
				.texture("Sampler0", texture)
				.textureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
				.build()
		)
	);

	private static final Function<Identifier, RenderLayer> STATIC_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.outlineMode(OutlineMode.NONE)
				.texture("Sampler0", texture)
				.textureTransform(SevenElementsRenderLayers.STATIC_ENTITY_GLINT_TEXTURING)
				.build()
		)
	);

	private static final SequencedMap<RenderLayer, BufferAllocator> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayers.WORLD_TEXT, SevenElementsRenderer.createAllocator(786432));
		}
	);

	private static final VertexConsumerProvider.Immediate WORLD_TEXT_IMMEDIATE = VertexConsumerProvider.immediate(WORLD_TEXT_SEQUENCED_MAP, SevenElementsRenderer.createAllocator(1536));

	public static RenderLayer getTriangles() {
		return SevenElementsRenderLayers.TRIANGLES;
	}

	public static RenderLayer getGaugeDisplay() {
		return SevenElementsRenderLayers.GAUGE_DISPLAY;
	}

	public static RenderLayer getLines() {
		return SevenElementsRenderLayers.RULER_LINES;
	}

	public static Function<Identifier, RenderLayer> getElements() {
		return SevenElementsRenderLayers.ELEMENTS;
	}

	public static RenderLayer getElements(Identifier texture) {
		return SevenElementsRenderLayers.ELEMENTS.apply(texture);
	}

	public static RenderLayer getWorldText() {
		return SevenElementsRenderLayers.WORLD_TEXT;
	}

	public static RenderLayer getSphere() {
		return SevenElementsRenderLayers.SPHERE;
	}

	public static RenderLayer getCrystallizeShield() {
		return SevenElementsRenderLayers.CRYSTALLIZE_SHIELD;
	}

	public static VertexConsumerProvider.Immediate getWorldTextImmediate() {
		return WORLD_TEXT_IMMEDIATE;
	}

	public static Function<Identifier, RenderLayer> getArmorEntityElementGlint() {
		return SevenElementsRenderLayers.ARMOR_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getArmorEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ARMOR_ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getElementGlint() {
		return SevenElementsRenderLayers.ELEMENT_GLINT;
	}

	public static RenderLayer getElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticElementGlint() {
		return SevenElementsRenderLayers.STATIC_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.STATIC_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getEntityElementGlint() {
		return SevenElementsRenderLayers.ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticEntityElementGlint() {
		return SevenElementsRenderLayers.STATIC_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.STATIC_ENTITY_ELEMENT_GLINT.apply(texture);
	}



	private static Matrix4f getStaticGlintTransformation(float scale) {
		Matrix4f matrix4f = new Matrix4f().translation(0.0F, 0.0F, 0.0F);
		matrix4f.rotateZ((float) (Math.PI / 18)).scale(scale);
		return matrix4f;
	}
}
