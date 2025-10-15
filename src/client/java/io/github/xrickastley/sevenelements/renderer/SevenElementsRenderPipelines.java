package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

public class SevenElementsRenderPipelines {
	private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
		.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
		.buildSnippet();

	public static final RenderPipeline TRIANGLES = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/triangles"))
			.build()
	);

	public static final RenderPipeline TRIANGLE_FAN = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/triangle_fan"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN)
			.build()
	);

	public static final RenderPipeline TRIANGLE_STRIP = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/triangle_strip"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
			.build()
	);

	public static final RenderPipeline QUADS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/quads"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
			.build()
	);

	public static final RenderPipeline LINES = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/lines"))
			.withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
			.withoutBlend()
			.withCull(false)
			.build()
	);

	public static final RenderPipeline ELEMENTS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/elements"))
			.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS)
			.withBlend(BlendFunction.TRANSLUCENT)
			.withCull(false)
			.build()
	);

	public static final RenderPipeline WORLD_TEXT = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/world_text"))
			.withVertexFormat(VertexFormats.POSITION_TEXTURE, DrawMode.QUADS)
			.withCull(false)
			.withDepthWrite(false)
			.build()
	);

	public static final RenderPipeline CHARGE_LINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/charge_line"))
			.withBlend(BlendFunction.TRANSLUCENT)
			.withCull(false)
			.build()
	);

	public static final RenderPipeline SPHERE = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/sphere"))
			.withCull(false)
			.withBlend(BlendFunction.PANORAMA)
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withDepthWrite(false)
			.build()
	);
}
