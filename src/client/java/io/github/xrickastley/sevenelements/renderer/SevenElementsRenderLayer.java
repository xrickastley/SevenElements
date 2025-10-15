package io.github.xrickastley.sevenelements.renderer;

import java.util.OptionalDouble;
import java.util.SequencedMap;
import java.util.function.Function;

import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderLayer.OutlineMode;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.LineWidth;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class SevenElementsRenderLayer {
	private static final RenderLayer TRIANGLES = RenderLayer.of(
		"seven-elements:triangles",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLES,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer TRIANGLE_FAN = RenderLayer.of(
		"seven-elements:triangle_fan",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLE_FAN,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer TRIANGLE_STRIP = RenderLayer.of(
		"seven-elements:triangle_strip",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLE_STRIP,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer QUADS = RenderLayer.of(
		"seven-elements:quads",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.QUADS,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer THIN_LINES = RenderLayer.of(
		"seven-elements:thin_lines",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.LINES,
		MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(5)))
			.build(OutlineMode.NONE)
	);

	private static final RenderLayer THICK_LINES = RenderLayer.of(
		"seven-elements:thick_lines",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.LINES,
		MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(10)))
			.build(OutlineMode.NONE)
	);

	private static final Function<Identifier, RenderLayer> ELEMENTS = Util.memoize(
		texture -> {
			MultiPhaseParameters multiPhaseParameters = MultiPhaseParameters.builder()
				.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
				.build(OutlineMode.NONE);

			return RenderLayer.of(
				"seven-elements:elements",
				RenderLayer.SOLID_BUFFER_SIZE,
				SevenElementsRenderPipelines.ELEMENTS,
				multiPhaseParameters
			);
		}
	);

	private static final RenderLayer WORLD_TEXT = RenderLayer.of(
		"seven-elements:world/text",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.WORLD_TEXT,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer INNER_CHARGE_LINE = RenderLayer.of(
		"seven-elements:world/charge_line/inner",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.CHARGE_LINE,
		MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(2.0)))
			.build(OutlineMode.NONE)
	);

	private static final RenderLayer OUTER_CHARGE_LINE = RenderLayer.of(
		"seven-elements:world/charge_line/outer",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.CHARGE_LINE,
		MultiPhaseParameters.builder()
			.lineWidth(new LineWidth(OptionalDouble.of(6.0)))
			.build(OutlineMode.NONE)
	);

	private static final RenderLayer SPHERE = RenderLayer.of(
		"seven-elements:sphere",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.SPHERE,
		MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final SequencedMap<RenderLayer, BufferAllocator> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayer.WORLD_TEXT, new BufferAllocator(RenderLayer.SOLID_BUFFER_SIZE));
		}
	);

	public static RenderLayer getTriangles() {
		return SevenElementsRenderLayer.TRIANGLES;
	}

	public static RenderLayer getTriangleFan() {
		return SevenElementsRenderLayer.TRIANGLE_FAN;
	}

	public static RenderLayer getTriangleStrip() {
		return SevenElementsRenderLayer.TRIANGLE_STRIP;
	}

	public static RenderLayer getQuads() {
		return SevenElementsRenderLayer.QUADS;
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

	public static RenderLayer getInnerChargeLine() {
		return SevenElementsRenderLayer.INNER_CHARGE_LINE;
	}

	public static RenderLayer getOuterChargeLine() {
		return SevenElementsRenderLayer.OUTER_CHARGE_LINE;
	}

	public static RenderLayer getSphere() {
		return SevenElementsRenderLayer.SPHERE;
	}

	public static VertexConsumerProvider.Immediate getWorldTextImmediate() {
		return VertexConsumerProvider.immediate(WORLD_TEXT_SEQUENCED_MAP, new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE));
	}
}
