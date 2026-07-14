package io.github.xrickastley.sevenelements.renderer;

import java.util.function.Function;

import org.joml.Matrix4f;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class SevenElementsRenderLayers {
	public static final TextureTransform STATIC_GLINT_TEXTURING = new TextureTransform(
		"seven-elements:static_glint_texturing", () -> getStaticGlintTransformation(8.0F)
	);

	public static final TextureTransform STATIC_ENTITY_GLINT_TEXTURING = new TextureTransform(
		"seven-elements:static_glint_texturing", () -> getStaticGlintTransformation(0.16F)
	);

	private static final RenderType TRIANGLES = RenderType.create(
		"seven-elements:triangles",
		RenderSetup
			.builder(SevenElementsRenderPipelines.TRIANGLES)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType GAUGE_DISPLAY = RenderType.create(
		"seven-elements:gauge_display",
		RenderSetup
			.builder(SevenElementsRenderPipelines.QUADS)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType RULER_LINES = RenderType.create(
		"seven-elements:ruler_lines",
		RenderSetup
			.builder(SevenElementsRenderPipelines.LINES)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ELEMENTS = Util.memoize(
		texture -> {
			return RenderType.create(
				"seven-elements:elements",
				RenderSetup
					.builder(SevenElementsRenderPipelines.ELEMENTS)
					.setOutline(OutlineProperty.NONE)
					.withTexture("Sampler0", texture)
					.createRenderSetup()
			);
		}
	);

	private static final RenderType WORLD_TEXT = RenderType.create(
		"seven-elements:world/text",
		RenderSetup
			.builder(SevenElementsRenderPipelines.WORLD_TEXT)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType SPHERE = RenderType.create(
		"seven-elements:sphere",
		RenderSetup
			.builder(SevenElementsRenderPipelines.SPHERE)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final RenderType CRYSTALLIZE_SHIELD = RenderType.create(
		"seven-elements:crystallize_shield",
		RenderSetup
			.builder(SevenElementsRenderPipelines.SPHERE)
			.setOutline(OutlineProperty.NONE)
			.createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ARMOR_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderType.create(
			"seven-elements:armor_entity_element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.setOutline(OutlineProperty.NONE)
				.withTexture("Sampler0", texture)
				.setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
				.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
				.createRenderSetup()
		)
	);

	public static final Function<Identifier, RenderType> ELEMENT_GLINT = Util.memoize(
		texture -> RenderType.create(
			"seven-elements:element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.setOutline(OutlineProperty.NONE)
				.withTexture("Sampler0", texture)
				.setTextureTransform(TextureTransform.GLINT_TEXTURING)
				.createRenderSetup()
		)
	);

	public static final Function<Identifier, RenderType> STATIC_ELEMENT_GLINT = Util.memoize(
		texture -> RenderType.create(
			"seven-elements:static_element_glint",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.setOutline(OutlineProperty.NONE)
				.withTexture("Sampler0", texture)
				.setTextureTransform(SevenElementsRenderLayers.STATIC_GLINT_TEXTURING)
				.createRenderSetup()
		)
	);

	private static final Function<Identifier, RenderType> ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderType.create(
			"seven-elements:entity_element_glint_direct",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.setOutline(OutlineProperty.NONE)
				.withTexture("Sampler0", texture)
				.setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
				.createRenderSetup()
		)
	);

	private static final Function<Identifier, RenderType> STATIC_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderType.create(
			"seven-elements:entity_element_glint_direct",
			RenderSetup
				.builder(RenderPipelines.GLINT)
				.setOutline(OutlineProperty.NONE)
				.withTexture("Sampler0", texture)
				.setTextureTransform(SevenElementsRenderLayers.STATIC_ENTITY_GLINT_TEXTURING)
				.createRenderSetup()
		)
	);

	public static RenderType getTriangles() {
		return SevenElementsRenderLayers.TRIANGLES;
	}

	public static RenderType getGaugeDisplay() {
		return SevenElementsRenderLayers.GAUGE_DISPLAY;
	}

	public static RenderType getLines() {
		return SevenElementsRenderLayers.RULER_LINES;
	}

	public static Function<Identifier, RenderType> getElements() {
		return SevenElementsRenderLayers.ELEMENTS;
	}

	public static RenderType getElements(Identifier texture) {
		return SevenElementsRenderLayers.ELEMENTS.apply(texture);
	}

	public static RenderType getWorldText() {
		return SevenElementsRenderLayers.WORLD_TEXT;
	}

	public static RenderType getSphere() {
		return SevenElementsRenderLayers.SPHERE;
	}

	public static RenderType getCrystallizeShield() {
		return SevenElementsRenderLayers.CRYSTALLIZE_SHIELD;
	}

	public static Function<Identifier, RenderType> getArmorEntityElementGlint() {
		return SevenElementsRenderLayers.ARMOR_ENTITY_ELEMENT_GLINT;
	}

	public static RenderType getArmorEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ARMOR_ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderType> getElementGlint() {
		return SevenElementsRenderLayers.ELEMENT_GLINT;
	}

	public static RenderType getElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderType> getStaticElementGlint() {
		return SevenElementsRenderLayers.STATIC_ELEMENT_GLINT;
	}

	public static RenderType getStaticElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.STATIC_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderType> getEntityElementGlint() {
		return SevenElementsRenderLayers.ENTITY_ELEMENT_GLINT;
	}

	public static RenderType getEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderType> getStaticEntityElementGlint() {
		return SevenElementsRenderLayers.STATIC_ENTITY_ELEMENT_GLINT;
	}

	public static RenderType getStaticEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayers.STATIC_ENTITY_ELEMENT_GLINT.apply(texture);
	}



	private static Matrix4f getStaticGlintTransformation(float scale) {
		Matrix4f matrix4f = new Matrix4f().translation(0.0F, 0.0F, 0.0F);
		matrix4f.rotateZ((float) (Math.PI / 18)).scale(scale);
		return matrix4f;
	}
}
