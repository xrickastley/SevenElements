package io.github.xrickastley.sevenelements.renderer;

import java.util.OptionalDouble;
import java.util.SequencedMap;
import java.util.function.Function;

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
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer TRIANGLE_FAN = RenderLayer.of(
		"seven-elements:triangle_fan",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLE_FAN,
		RenderLayer.MultiPhaseParameters.builder().build(OutlineMode.NONE)
	);

	private static final RenderLayer TRIANGLE_STRIP = RenderLayer.of(
		"seven-elements:triangle_strip",
		RenderLayer.SOLID_BUFFER_SIZE,
		SevenElementsRenderPipelines.TRIANGLE_STRIP,
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
				.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
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

	private static final SequencedMap<RenderLayer, BufferAllocator> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayer.WORLD_TEXT, SevenElementsRenderer.createAllocator(RenderLayer.SOLID_BUFFER_SIZE));
		}
	);

	private static final VertexConsumerProvider.Immediate WORLD_TEXT_IMMEDIATE = VertexConsumerProvider.immediate(WORLD_TEXT_SEQUENCED_MAP, SevenElementsRenderer.createAllocator(RenderLayer.DEFAULT_BUFFER_SIZE));

	public static RenderLayer getTriangles() {
		return SevenElementsRenderLayer.TRIANGLES;
	}

	public static RenderLayer getTriangleFan() {
		return SevenElementsRenderLayer.TRIANGLE_FAN;
	}

	public static RenderLayer getTriangleStrip() {
		return SevenElementsRenderLayer.TRIANGLE_STRIP;
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
}
