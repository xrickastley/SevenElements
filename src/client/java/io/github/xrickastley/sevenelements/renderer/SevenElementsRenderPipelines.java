package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.client.renderer.RenderPipelines;

public class SevenElementsRenderPipelines {
	private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
		.buildSnippet();

	public static final RenderPipeline TRIANGLES = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/triangles"))
			.build()
	);

	public static final RenderPipeline QUADS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/quads"))
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
			.build()
	);

	public static final RenderPipeline LINES = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/lines"))
			.withoutBlend()
			.withCull(false)
			.build()
	);

	public static final RenderPipeline CIRCLE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/circle"))
			.withFragmentShader(SevenElements.identifier("circle"))
			.build()
	);

	public static final RenderPipeline ELEMENTS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/elements"))
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS)
			.withBlend(BlendFunction.TRANSLUCENT)
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withCull(true)
			.build()
	);

	public static final RenderPipeline WORLD_TEXT = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/world_text"))
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX, Mode.QUADS)
			.withCull(false)
			.withDepthWrite(false)
			.build()
	);

	public static final RenderPipeline CHARGE_LINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/charge_line"))
			.withBlend(BlendFunction.TRANSLUCENT)
			.withCull(false)
			.build()
	);

	public static final RenderPipeline SPHERE = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/sphere"))
			.withCull(false)
			.withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO))
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withDepthWrite(false)
			.build()
	);
}
