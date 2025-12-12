package io.github.xrickastley.sevenelements.renderer;

import java.util.SequencedMap;
import java.util.function.Function;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup.OutlineMode;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class SevenElementsRenderLayer {
	private static final RenderLayer TRIANGLES = RenderLayer.of(
		"seven-elements:triangles",
		RenderSetup
			.builder(SevenElementsRenderPipelines.TRIANGLES)
			.outlineMode(OutlineMode.NONE)
			.build()
	);

	private static final RenderLayer QUADS = RenderLayer.of(
		"seven-elements:quads",
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

	private static final RenderLayer CHARGE_LINE = RenderLayer.of(
		"seven-elements:world/charge_line",
		RenderSetup
			.builder(SevenElementsRenderPipelines.CHARGE_LINE)
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

	private static final SequencedMap<RenderLayer, BufferAllocator> WORLD_TEXT_SEQUENCED_MAP = Util.make(
		new Object2ObjectLinkedOpenHashMap<>(), map -> {
			map.put(SevenElementsRenderLayer.WORLD_TEXT, SevenElementsRenderer.createAllocator(786432));
		}
	);

	private static final VertexConsumerProvider.Immediate WORLD_TEXT_IMMEDIATE = VertexConsumerProvider.immediate(WORLD_TEXT_SEQUENCED_MAP, SevenElementsRenderer.createAllocator(1536));

	public static RenderLayer getTriangles() {
		return SevenElementsRenderLayer.TRIANGLES;
	}

	public static RenderLayer getQuads() {
		return SevenElementsRenderLayer.QUADS;
	}

	public static RenderLayer getLines() {
		return SevenElementsRenderLayer.RULER_LINES;
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

	public static RenderLayer getChargeLine() {
		return SevenElementsRenderLayer.CHARGE_LINE;
	}

	public static RenderLayer getSphere() {
		return SevenElementsRenderLayer.SPHERE;
	}

	public static VertexConsumerProvider.Immediate getWorldTextImmediate() {
		return WORLD_TEXT_IMMEDIATE;
	}
}
