package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.function.Function;

import org.joml.Matrix4f;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SevenElementsRenderLayer extends RenderLayer {
	private SevenElementsRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}

	public static final RenderPhase.Texturing STATIC_GLINT_TEXTURING = new RenderPhase.Texturing(
		"seven-elements:static_glint_texturing", () -> setupStaticGlintTexturing(8.0F), () -> RenderSystem.resetTextureMatrix()
	);

	public static final RenderPhase.Texturing STATIC_ENTITY_GLINT_TEXTURING = new RenderPhase.Texturing(
		"seven-elements:static_glint_texturing", () -> setupStaticGlintTexturing(0.16F), () -> RenderSystem.resetTextureMatrix()
	);

	private static final Function<Identifier, RenderLayer> ARMOR_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:armor_element_glint",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			256,
			MultiPhaseParameters.builder()
				.program(RenderPhase.ARMOR_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(RenderPhase.GLINT_TEXTURING)
				.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> ARMOR_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:armor_entity_element_glint",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			MultiPhaseParameters.builder()
				.program(RenderPhase.ARMOR_ENTITY_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
				.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
				.build(false)
		)
	);

	public static final Function<Identifier, RenderLayer> ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_glint",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			MultiPhaseParameters.builder()
				.program(RenderPhase.GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(RenderPhase.GLINT_TEXTURING)
				.build(false)
		)
	);

	public static final Function<Identifier, RenderLayer> STATIC_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:static_element_glint",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			MultiPhaseParameters.builder()
				.program(RenderPhase.GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(SevenElementsRenderLayer.STATIC_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.ENTITY_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.target(RenderPhase.ITEM_ENTITY_TARGET)
				.texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> STATIC_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.ENTITY_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.target(RenderPhase.ITEM_ENTITY_TARGET)
				.texturing(SevenElementsRenderLayer.STATIC_ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> DIRECT_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			256,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.DIRECT_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
				.writeMaskState(COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(RenderPhase.GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> STATIC_DIRECT_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			256,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.DIRECT_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
				.writeMaskState(COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.texturing(RenderPhase.GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> DIRECT_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.DIRECT_ENTITY_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.target(RenderPhase.ITEM_ENTITY_TARGET)
				.texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	private static final Function<Identifier, RenderLayer> STATIC_DIRECT_ENTITY_ELEMENT_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:entity_element_glint_direct",
			VertexFormats.POSITION_TEXTURE,
			VertexFormat.DrawMode.QUADS,
			1536,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderPhase.DIRECT_ENTITY_GLINT_PROGRAM)
				.texture(new RenderPhase.Texture(texture, true, false))
				.writeMaskState(RenderPhase.COLOR_MASK)
				.cull(RenderPhase.DISABLE_CULLING)
				.depthTest(RenderPhase.EQUAL_DEPTH_TEST)
				.transparency(RenderPhase.GLINT_TRANSPARENCY)
				.target(RenderPhase.ITEM_ENTITY_TARGET)
				.texturing(SevenElementsRenderLayer.STATIC_ENTITY_GLINT_TEXTURING)
				.build(false)
		)
	);

	public static Function<Identifier, RenderLayer> getArmorElementGlint() {
		return SevenElementsRenderLayer.ARMOR_ELEMENT_GLINT;
	}

	public static RenderLayer getArmorElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.ARMOR_ELEMENT_GLINT.apply(texture);
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

	public static Function<Identifier, RenderLayer> getDirectElementGlint() {
		return SevenElementsRenderLayer.DIRECT_ELEMENT_GLINT;
	}

	public static RenderLayer getDirectElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.DIRECT_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticDirectElementGlint() {
		return SevenElementsRenderLayer.STATIC_DIRECT_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticDirectElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.STATIC_DIRECT_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getDirectEntityElementGlint() {
		return SevenElementsRenderLayer.DIRECT_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getDirectEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.DIRECT_ENTITY_ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getStaticDirectEntityElementGlint() {
		return SevenElementsRenderLayer.STATIC_DIRECT_ENTITY_ELEMENT_GLINT;
	}

	public static RenderLayer getStaticDirectEntityElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.STATIC_DIRECT_ENTITY_ELEMENT_GLINT.apply(texture);
	}



	private static void setupStaticGlintTexturing(float scale) {
		Matrix4f matrix4f = new Matrix4f().translation(0.0F, 0.0F, 0.0F);
		matrix4f.rotateZ((float) (Math.PI / 18)).scale(scale);
		RenderSystem.setTextureMatrix(matrix4f);
	}
}
