package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.Optional;

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
			.withColorTargetState(ColorTargetState.DEFAULT)
			.withCull(false)
			.build()
	);

	public static final RenderPipeline CIRCLE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/circle"))
			.withFragmentShader(SevenElements.identifier("circle"))
			.build()
	);

	public static final RenderPipeline ELEMENTS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/elements"))
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
			.withCull(true)
			.build()
	);

	public static final RenderPipeline WORLD_TEXT = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/world_text"))
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX, Mode.QUADS)
			.withDepthStencilState(Optional.empty())
			.withCull(false)
			.build()
	);

	public static final RenderPipeline CHARGE_LINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/charge_line"))
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.build()
	);

	public static final RenderPipeline SPHERE = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/sphere"))
			.withCull(false)
			.withColorTargetState(
				new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO))
			)
			.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
			.build()
	);
}
