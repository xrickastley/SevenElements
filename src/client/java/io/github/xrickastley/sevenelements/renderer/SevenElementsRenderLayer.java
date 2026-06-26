package io.github.xrickastley.sevenelements.renderer;

import java.util.function.Function;

import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SevenElementsRenderLayer {
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

	private static final Function<Identifier, RenderLayer> ELEMENT_ARMOR_ENTITY_GLINT = Util.memoize(
		texture -> RenderLayer.of(
			"seven-elements:element_armor_entity_glint",
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

	public static Function<Identifier, RenderLayer> getElementGlint() {
		return SevenElementsRenderLayer.ELEMENT_GLINT;
	}

	public static RenderLayer getElementGlint(Identifier texture) {
		return SevenElementsRenderLayer.ELEMENT_GLINT.apply(texture);
	}

	public static Function<Identifier, RenderLayer> getElementArmorEntityGlint() {
		return SevenElementsRenderLayer.ELEMENT_ARMOR_ENTITY_GLINT;
	}

	public static RenderLayer getElementArmorEntityGlint(Identifier texture) {
		return SevenElementsRenderLayer.ELEMENT_ARMOR_ENTITY_GLINT.apply(texture);
	}
}
