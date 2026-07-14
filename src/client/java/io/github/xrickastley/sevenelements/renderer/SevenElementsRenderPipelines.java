package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import java.util.Optional;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.client.renderer.RenderPipelines;

public class SevenElementsRenderPipelines {
	private static final RenderPipeline.Snippet TRIANGLES_SNIPPET = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.buildSnippet();

	public static final RenderPipeline TRIANGLES = RenderPipelines.register(
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/triangles"))
			.build()
	);

	public static final RenderPipeline QUADS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/quads"))
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withDepthStencilState(DepthStencilState.DEFAULT)
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
		RenderPipeline.builder(SevenElementsRenderPipelines.TRIANGLES_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/circle"))
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.build()
	);

	public static final RenderPipeline ELEMENTS = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SevenElements.identifier("pipeline/elements"))
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(true)
			.build()
	);

	public static final RenderPipeline WORLD_TEXT = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
			.withLocation("pipeline/world_text")
			.withVertexShader("core/text")
			.withFragmentShader("core/text")
			.withShaderDefine("IS_GRAYSCALE")
			.withShaderDefine("IS_SEE_THROUGH")
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
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.ONE, BlendFactor.ZERO)))
			.withDepthStencilState(Optional.empty())
			.build()
	);
}
